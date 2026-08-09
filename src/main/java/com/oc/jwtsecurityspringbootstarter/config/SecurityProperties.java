package com.oc.jwtsecurityspringbootstarter.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "auth.security")
public class SecurityProperties {
    private final boolean enabled;
    private final String jwtAccessSecret;
    private final String jwtRefreshSecret;
    private final long jwtAccessExpiration;
    private final long jwtRefreshExpiration;
    private final List<String> publicEndpoints;
    private final ApiPaths api;
    private final CorsConfig cors;

    @Getter
    @RequiredArgsConstructor
    public static class ApiPaths {
        private final String login;
        private final String register;
        private final String refresh;
    }

    @Getter
    @RequiredArgsConstructor
    public static class CorsConfig {
        private final List<String> allowedOrigins;
        private final List<String> allowedMethods;
        private final List<String> allowedHeaders;
    }
}