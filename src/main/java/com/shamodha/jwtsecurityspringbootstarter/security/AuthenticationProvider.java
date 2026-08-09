package com.shamodha.jwtsecurityspringbootstarter.security;

import com.shamodha.jwtsecurityspringbootstarter.model.UserContext;

public interface AuthenticationProvider {
    UserContext authenticate(String username, String password);
}