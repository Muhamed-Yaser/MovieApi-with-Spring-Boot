
package com.netflix.MovieAPI.auth.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "forgot_password")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPassword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer fpid;

    @Column(length = 100)
    private String otpHash;

    private Instant otpExpirationTime;

    @Column(nullable = false)
    @Builder.Default
    private Integer attempts = 0;

    @Column(length = 64, unique = true)
    private String resetTokenHash;

    private Instant resetTokenExpirationTime;

    private Instant lastRequestedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}
