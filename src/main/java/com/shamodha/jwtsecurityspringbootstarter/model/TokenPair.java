package com.shamodha.jwtsecurityspringbootstarter.model;

public record TokenPair(
        String accessToken,
        String refreshToken,
        long expiresIn
) {}