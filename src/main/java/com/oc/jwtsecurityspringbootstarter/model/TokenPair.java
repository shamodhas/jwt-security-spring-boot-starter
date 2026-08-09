package com.oc.jwtsecurityspringbootstarter.model;

public record TokenPair(
        String accessToken,
        String refreshToken,
        long expiresIn
) {}