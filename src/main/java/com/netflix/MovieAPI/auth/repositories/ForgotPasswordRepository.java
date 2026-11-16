
package com.netflix.MovieAPI.auth.repositories;

import com.netflix.MovieAPI.auth.entities.ForgotPassword;
import com.netflix.MovieAPI.auth.entities.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ForgotPasswordRepository
        extends JpaRepository<ForgotPassword, Integer> {

    Optional<ForgotPassword> findByUser(User user);

    @EntityGraph(attributePaths = "user")
    Optional<ForgotPassword> findByResetTokenHash(String resetTokenHash);
}
