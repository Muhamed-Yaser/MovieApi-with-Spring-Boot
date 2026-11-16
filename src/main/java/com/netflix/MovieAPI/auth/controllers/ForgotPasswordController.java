
package com.netflix.MovieAPI.auth.controllers;

import com.netflix.MovieAPI.auth.services.ForgotPasswordService;
import com.netflix.MovieAPI.auth.utils.RequestPasswordReset;
import com.netflix.MovieAPI.auth.utils.ResetPasswordRequest;
import com.netflix.MovieAPI.auth.utils.ResetTokenResponse;
import com.netflix.MovieAPI.auth.utils.VerifyOtpRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/forgotPassword")
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final ForgotPasswordService forgotPasswordService;

    @PostMapping("/request")
    public ResponseEntity<String> requestPasswordReset(
            @Valid @RequestBody RequestPasswordReset request) {

        forgotPasswordService.requestPasswordReset(request);

        return ResponseEntity.ok(
                "A password reset OTP has been sent."
        );
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ResetTokenResponse> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        String resetToken = forgotPasswordService.verifyOtp(request);

        return ResponseEntity.ok(
                new ResetTokenResponse(resetToken)
        );
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        forgotPasswordService.resetPassword(request);

        return ResponseEntity.ok("Password has been changed successfully.");
    }
}
