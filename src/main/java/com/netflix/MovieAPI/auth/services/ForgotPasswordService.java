
package com.netflix.MovieAPI.auth.services;

import com.netflix.MovieAPI.auth.entities.ForgotPassword;
import com.netflix.MovieAPI.auth.entities.User;
import com.netflix.MovieAPI.auth.repositories.ForgotPasswordRepository;
import com.netflix.MovieAPI.auth.repositories.RefreshTokenRepository;
import com.netflix.MovieAPI.auth.repositories.UserRepository;
import com.netflix.MovieAPI.dto.MailBody;
import com.netflix.MovieAPI.auth.utils.ResetPasswordRequest;
import com.netflix.MovieAPI.auth.utils.RequestPasswordReset;
import com.netflix.MovieAPI.auth.utils.VerifyOtpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class ForgotPasswordService {

    private final UserRepository userRepository;
    private final ForgotPasswordRepository forgotPasswordRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final long OTP_EXPIRATION_MINUTES = 10;
    private static final long RESET_TOKEN_EXPIRATION_MINUTES = 10;
    private static final long RESEND_COOLDOWN_SECONDS = 60;

    @Transactional
    public void requestPasswordReset(RequestPasswordReset request) {

        User user = userRepository.findByEmail(request.email()).orElse(null);

        // Same response for existing and not found emails.
        if (user == null) {
            return;
        }

        ForgotPassword forgotPassword = forgotPasswordRepository
                .findByUser(user)
                .orElseGet(() -> ForgotPassword.builder()
                        .user(user)
                        .attempts(0)
                        .build());

        Instant now = Instant.now();

        if (forgotPassword.getLastRequestedAt() != null
                && forgotPassword.getLastRequestedAt()
                .plusSeconds(RESEND_COOLDOWN_SECONDS)
                .isAfter(now)) {
            return;
        }

        String otp = generateOtp();

        forgotPassword.setOtpHash(passwordEncoder.encode(otp));
        forgotPassword.setOtpExpirationTime(
                now.plus(OTP_EXPIRATION_MINUTES, ChronoUnit.MINUTES)
        );
        forgotPassword.setAttempts(0);
        forgotPassword.setResetTokenHash(null);
        forgotPassword.setResetTokenExpirationTime(null);
        forgotPassword.setLastRequestedAt(now);

        forgotPasswordRepository.save(forgotPassword);

        MailBody mailBody = MailBody.builder()
                .to(user.getEmail())
                .subject("MovieAPI - Password Reset OTP")
                .text("Your password reset OTP is: " + otp
                        + "\nIt expires in 10 minutes."
                        + "\nIf you did not request this, ignore this email.")
                .build();

        emailService.sendSimpleMessage(mailBody);
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public String verifyOtp(VerifyOtpRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid or expired OTP"
                ));

        ForgotPassword forgotPassword = forgotPasswordRepository
                .findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid or expired OTP"
                ));

        Instant now = Instant.now();

        if (forgotPassword.getOtpHash() == null
                || forgotPassword.getOtpExpirationTime() == null
                || !forgotPassword.getOtpExpirationTime().isAfter(now)) {

            forgotPasswordRepository.delete(forgotPassword);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid or expired OTP"
            );
        }

        if (forgotPassword.getAttempts() >= MAX_OTP_ATTEMPTS) {
            forgotPasswordRepository.delete(forgotPassword);

            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too many OTP attempts. Request a new OTP."
            );
        }

        if (!passwordEncoder.matches(
                request.otp(),
                forgotPassword.getOtpHash()
        )) {
            forgotPassword.setAttempts(forgotPassword.getAttempts() + 1);

            if (forgotPassword.getAttempts() >= MAX_OTP_ATTEMPTS) {
                forgotPasswordRepository.delete(forgotPassword);
            }

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid or expired OTP"
            );
        }

        String resetToken = generateResetToken();

        forgotPassword.setOtpHash(null);
        forgotPassword.setOtpExpirationTime(null);
        forgotPassword.setResetTokenHash(hashToken(resetToken));
        forgotPassword.setResetTokenExpirationTime(
                now.plus(RESET_TOKEN_EXPIRATION_MINUTES, ChronoUnit.MINUTES)
        );

        forgotPasswordRepository.save(forgotPassword);

        return resetToken;
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        if (!request.password().equals(request.repeatPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Passwords do not match"
            );
        }

        String tokenHash = hashToken(request.resetToken());

        ForgotPassword forgotPassword = forgotPasswordRepository
                .findByResetTokenHash(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid or expired reset token"
                ));

        if (forgotPassword.getResetTokenExpirationTime() == null
                || !forgotPassword.getResetTokenExpirationTime()
                .isAfter(Instant.now())) {

            forgotPasswordRepository.delete(forgotPassword);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid or expired reset token"
            );
        }

        User user = forgotPassword.getUser();

        String encodedPassword = passwordEncoder.encode(request.password());

        int updatedRows = userRepository.updatePassword(
                user.getEmail(),
                encodedPassword
        );

        if (updatedRows != 1) {
            throw new IllegalStateException("Could not update user password");
        }

        // delete all refresh-token sessions for this user.
        refreshTokenRepository.deleteAllByUser(user);

        // Reset token become unusable after password change.
        forgotPasswordRepository.delete(forgotPassword);
    }

    private String generateOtp() {
        return String.format(
                "%06d",
                SECURE_RANDOM.nextInt(1_000_000)
        );
    }

    private String generateResetToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (Exception e) {
            throw new IllegalStateException("Could not hash reset token", e);
        }
    }
}
