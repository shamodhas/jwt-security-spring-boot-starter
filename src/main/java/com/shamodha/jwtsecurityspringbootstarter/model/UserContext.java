package com.shamodha.jwtsecurityspringbootstarter.model;

import java.util.List;

public record UserContext(
        String userId,
        String username,
        String email,
        List<String> roles
) {
    public <T extends Enum<T>> List<T> getRolesAs(Class<T> enumType) {
        return roles.stream()
                .map(role -> Enum.valueOf(enumType, role.toUpperCase()))
                .toList();
    }

    public <T extends Enum<T>> boolean hasRole(T role) {
        return roles.stream().anyMatch(r -> r.equalsIgnoreCase(role.name()));
    }
}