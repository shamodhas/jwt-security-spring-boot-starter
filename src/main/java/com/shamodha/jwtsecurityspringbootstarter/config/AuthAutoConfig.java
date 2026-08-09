package com.shamodha.jwtsecurityspringbootstarter.config;

import com.shamodha.jwtsecurityspringbootstarter.annotation.PublicApi;
import com.shamodha.jwtsecurityspringbootstarter.core.JwtEngine;
import com.shamodha.jwtsecurityspringbootstarter.filter.JwtAuthFilter;
import com.shamodha.jwtsecurityspringbootstarter.filter.RestAuthenticationEntryPoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.List;

@AutoConfiguration
@EnableMethodSecurity
@ConditionalOnProperty(prefix = "auth.security", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SecurityProperties.class)
public class AuthAutoConfig {

    @Bean
    @ConditionalOnMissingBean
    public JwtEngine jwtEngine(SecurityProperties properties) {
        return new JwtEngine(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtAuthFilter jwtAuthFilter(JwtEngine jwtEngine) {
        return new JwtAuthFilter(jwtEngine);
    }

    @Bean
    @ConditionalOnMissingBean
    public CorsConfigurationSource corsConfigurationSource(SecurityProperties properties) {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(properties.cors().allowedOrigins());
        configuration.setAllowedMethods(properties.cors().allowedMethods());
        configuration.setAllowedHeaders(properties.cors().allowedHeaders());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityProperties properties,
            JwtAuthFilter jwtFilter,
            ApplicationContext applicationContext) throws Exception {

        List<String> publicPaths = new ArrayList<>(properties.publicEndpoints());
        publicPaths.add(properties.api().login());
        publicPaths.add(properties.api().register());
        publicPaths.add(properties.api().refresh());

        try {
            RequestMappingHandlerMapping mapping = applicationContext.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
            mapping.getHandlerMethods().forEach((info, method) -> {
                if (method.hasMethodAnnotation(PublicApi.class) || method.getBeanType().isAnnotationPresent(PublicApi.class)) {
                    if (info.getPathPatternsCondition() != null) {
                        publicPaths.addAll(info.getPathPatternsCondition().getPatternValues());
                    }
                }
            });
        } catch (Exception ignored) {
        }

        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource(properties)))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new RestAuthenticationEntryPoint()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(publicPaths.toArray(new String[0])).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}