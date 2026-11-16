package com.netflix.MovieAPI.auth.services;

import com.netflix.MovieAPI.auth.entities.RefreshToken;
import com.netflix.MovieAPI.auth.entities.User;
import com.netflix.MovieAPI.auth.repositories.RefreshTokenRepository;
import com.netflix.MovieAPI.auth.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken createRefreshToken(String username) {

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + username));

        String rawToken = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .refreshToken(hashToken(rawToken))
                .expirationTime(Instant.now().plus(7, ChronoUnit.DAYS))
                .sessionId(UUID.randomUUID().toString())
                .user(user)
                .build();

        refreshTokenRepository.save(refreshToken);

        refreshToken.setRawToken(rawToken);

        return refreshToken;
    }

    @Transactional
    public RefreshToken rotateRefreshToken(String rawToken) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByRefreshToken(hashToken(rawToken))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Refresh token is invalid"));

        if (!refreshToken.getExpirationTime().isAfter(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("Refresh token expired");
        }

        String newRawToken = UUID.randomUUID().toString();

        refreshToken.setRefreshToken(hashToken(newRawToken));
        refreshToken.setExpirationTime(Instant.now().plus(7, ChronoUnit.DAYS));
        refreshToken.setRawToken(newRawToken);

        return refreshTokenRepository.save(refreshToken);
    }

    public void logout(String sessionId) {

        refreshTokenRepository.findBySessionId(sessionId)
                .ifPresent(refreshTokenRepository::delete);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}