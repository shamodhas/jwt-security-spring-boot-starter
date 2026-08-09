package com.oc.jwtsecurityspringbootstarter.security;

import com.oc.jwtsecurityspringbootstarter.model.UserContext;

public interface AuthenticationProvider {
    UserContext authenticate(String username, String password);
}