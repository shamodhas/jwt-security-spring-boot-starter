package com.oc.jwtsecurityspringbootstarter.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "auth.security")
public class SecurityProperties {
    private boolean enabled = true;
    private String jwtAccessSecret;
    private String jwtRefreshSecret;
    private long jwtAccessExpiration = 86400000;
    private long jwtRefreshExpiration = 604800000;
    private List<String> publicEndpoints = new ArrayList<>();
    private ApiPaths api = new ApiPaths();
    private CorsConfig cors = new CorsConfig();

    @Getter
    @Setter
    public static class ApiPaths {
        private String login = "/api/v1/auth/login";
        private String register = "/api/v1/auth/register";
        private String refresh = "/api/v1/auth/refresh";
    }

    @Getter
    @Setter
    public static class CorsConfig {
        private List<String> allowedOrigins = List.of("*");
        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");
        private List<String> allowedHeaders = List.of("*");
    }
}