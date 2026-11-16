package com.netflix.MovieAPI.auth.repositories;

import com.netflix.MovieAPI.auth.entities.RefreshToken;
import com.netflix.MovieAPI.auth.entities.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    @EntityGraph(attributePaths = "user")
    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    Optional<RefreshToken> findBySessionId(String sessionId);

    void deleteAllByUser(User user);

}