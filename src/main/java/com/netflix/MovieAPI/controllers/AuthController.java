package com.netflix.MovieAPI.controllers;


import com.netflix.MovieAPI.auth.entities.RefreshToken;
import com.netflix.MovieAPI.auth.entities.User;
import com.netflix.MovieAPI.auth.services.AuthService;
import com.netflix.MovieAPI.auth.services.JwtService;
import com.netflix.MovieAPI.auth.services.RefreshTokenService;
import com.netflix.MovieAPI.auth.utils.AuthResponse;
import com.netflix.MovieAPI.auth.utils.LoginRequest;
import com.netflix.MovieAPI.auth.utils.RefreshTokenRequest;
import com.netflix.MovieAPI.auth.utils.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, RefreshTokenService refreshTokenService, JwtService jwtService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register( @Valid @RequestBody RegisterRequest registerRequest) {
        System.out.println(">>> REGISTER ENDPOINT REACHED");

        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        System.out.println(">>> LOGIN CONTROLLER REACHED");
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {

        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(
                refreshTokenRequest.getRefreshToken()
        );
        User user = refreshToken.getUser();

        String accessToken = jwtService.generateToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getRefreshToken())
                .build());
    }
}