package com.oc.jwtsecurityspringbootstarter.core;

import com.oc.jwtsecurityspringbootstarter.config.SecurityProperties;
import com.oc.jwtsecurityspringbootstarter.model.TokenPair;
import com.oc.jwtsecurityspringbootstarter.model.UserContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtEngine {
    private final SecurityProperties properties;

    public JwtEngine(SecurityProperties properties) {
        this.properties = properties;
    }

    private SecretKey getAccessKey() {
        return Keys.hmacShaKeyFor(properties.getJwtAccessSecret().getBytes(StandardCharsets.UTF_8));
    }

    private SecretKey getRefreshKey() {
        return Keys.hmacShaKeyFor(properties.getJwtRefreshSecret().getBytes(StandardCharsets.UTF_8));
    }

    public TokenPair generateTokenPair(UserContext context) {
        long now = System.currentTimeMillis();

        String accessToken = Jwts.builder()
                .subject(context.userId())
                .claim("username", context.username())
                .claim("email", context.email())
                .claim("roles", context.roles())
                .issuedAt(new Date(now))
                .expiration(new Date(now + properties.getJwtAccessExpiration()))
                .signWith(getAccessKey())
                .compact();

        String refreshToken = Jwts.builder()
                .subject(context.userId())
                .issuedAt(new Date(now))
                .expiration(new Date(now + properties.getJwtRefreshExpiration()))
                .signWith(getRefreshKey())
                .compact();

        return new TokenPair(accessToken, refreshToken, properties.getJwtAccessExpiration());
    }

    public boolean isAccessTokenValid(String token) {
        try {
            Jwts.parser().verifyWith(getAccessKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public UserContext extractContext(String token) {
        Claims claims = Jwts.parser().verifyWith(getAccessKey()).build().parseSignedClaims(token).getPayload();
        String userId = claims.getSubject();
        String username = claims.get("username", String.class);
        String email = claims.get("email", String.class);

        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        return new UserContext(userId, username, email, roles);
    }
}