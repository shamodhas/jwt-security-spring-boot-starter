package com.shamodha.jwtsecurityspringbootstarter.security;

import com.shamodha.jwtsecurityspringbootstarter.model.UserContext;

import java.util.List;
import java.util.Optional;

public interface AuthenticationProvider {
    Optional<UserCredential> loadUserByUsername(String username);

    record UserCredential(
            String userId,
            String username,
            String email,
            String encodedPassword,
            List<String> roles
    ) {
    }
}