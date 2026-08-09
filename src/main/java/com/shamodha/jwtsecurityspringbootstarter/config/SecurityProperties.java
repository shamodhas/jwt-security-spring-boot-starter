package com.shamodha.jwtsecurityspringbootstarter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

@ConfigurationProperties(prefix = "auth.security")
public record SecurityProperties(@DefaultValue("true") boolean enabled, String jwtAccessSecret, String jwtRefreshSecret,
                                 @DefaultValue("86400000") long jwtAccessExpiration,
                                 @DefaultValue("604800000") long jwtRefreshExpiration,
                                 @DefaultValue List<String> publicEndpoints, @DefaultValue ApiPaths api,
                                 @DefaultValue CorsConfig cors) {

    public record ApiPaths(@DefaultValue("/api/v1/auth/login") String login,
                           @DefaultValue("/api/v1/auth/register") String register,
                           @DefaultValue("/api/v1/auth/refresh") String refresh) {
    }

    public record CorsConfig(@DefaultValue("*") List<String> allowedOrigins,
                             @DefaultValue({"GET", "POST", "PUT", "DELETE", "OPTIONS"}) List<String> allowedMethods,
                             @DefaultValue("*") List<String> allowedHeaders) {
    }
}