
package com.netflix.MovieAPI.auth.utils;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RequestPasswordReset(

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email

) {}
