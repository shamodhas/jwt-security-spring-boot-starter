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

    public record ApiPaths(String login, String register, String refresh) {
    }

    public record CorsConfig(List<String> allowedOrigins, List<String> allowedMethods, List<String> allowedHeaders) {
    }
}