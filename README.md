# JWT Security Spring Boot Starter

[![License](https://img.shields.io/github/license/shamodhas/jwt-security-spring-boot-starter)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21%2B-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen)](https://spring.io/projects/spring-boot)
[![Build](https://img.shields.io/github/actions/workflow/status/shamodhas/jwt-security-spring-boot-starter/build.yml?branch=main)](https://github.com/shamodhas/jwt-security-spring-boot-starter/actions)
[![Issues](https://img.shields.io/github/issues/shamodhas/jwt-security-spring-boot-starter)](https://github.com/shamodhas/jwt-security-spring-boot-starter/issues)
[![Stars](https://img.shields.io/github/stars/shamodhas/jwt-security-spring-boot-starter?style=social)](https://github.com/shamodhas/jwt-security-spring-boot-starter)

`jwt-security-spring-boot-starter`

An enterprise-grade, secure-by-default Spring Boot starter library providing stateless JSON Web Token (JWT) authentication, role-based access control, automatic `@PublicApi` discovery, pluggable SPI persistence adapters, and flexible configuration management.

Drop it into any Spring Boot 3 project, wire up one interface for your user store, and get a fully working, production-grade authentication layer — no boilerplate `SecurityFilterChain`, no hand-rolled JWT filters.

---

## Table of Contents

- [Why This Library](#why-this-library)
- [Features](#features)
- [Installation & Requirements](#installation--requirements)
- [Quick Start](#quick-start)
- [Configuration](#configuration-applicationyml)
- [Usage Guide](#usage-guide)
  - [1. Whitelisting Public Endpoints](#1-whitelisting-public-endpoints)
  - [2. Protecting Endpoints by Role](#2-protecting-endpoints-by-role)
  - [3. Injecting User Context in Controllers](#3-injecting-user-context-in-controllers)
- [Database Connectivity (SPI)](#database-connectivity-spi)
- [Security Best Practices](#security-best-practices)
- [Troubleshooting / FAQ](#troubleshooting--faq)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)

---

## Why This Library

Most teams re-implement the same JWT authentication filter, whitelist logic, and role-based access boilerplate on every new Spring Boot project. This starter packages that logic once, tested and secure by default, so you only need to:

1. Add the dependency.
2. Set your secrets and expirations in `application.yml`.
3. Implement one interface (`AuthenticationProvider`) to connect your user store.

Everything else — filters, token issuing/validation, CORS, public-route discovery — is handled for you.

---

## Features

- **Secure-by-Default** — Enforces authentication on all routes automatically unless explicitly whitelisted.
- **Automatic `@PublicApi` Discovery** — Scans your Spring MVC controllers at startup and automatically bypasses security for any endpoint or controller annotated with `@PublicApi`.
- **Stateless JWT Engine** — Robust cryptographic generation and verification for Access and Refresh tokens using JJWT.
- **Configurable Properties** — Clean, immutable configuration records bound to `auth.security`.
- **CORS Ready** — Built-in, configurable Cross-Origin Resource Sharing handler.

---

## Installation & Requirements

**Requirements:**

- Java 21 or higher
- Spring Boot 3.3.0

**Build & install to your local Maven repository:**

```powershell
$env:JAVA_HOME="C:\Users\shamo\.jdks\corretto-21.0.11"
.\mvnw clean install
```

Then add the dependency to your project's `pom.xml`:

```xml
<dependency>
    <groupId>com.oc</groupId>
    <artifactId>jwt-security-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

> Replace the `groupId`, `artifactId`, and `version` with the values published in your local repository.

**Or with Gradle:**

```groovy
implementation 'com.oc:jwt-security-spring-boot-starter:1.0.0'
```

> Once published to Maven Central or a public repository like JitPack, update this section with the real coordinates and a badge linking to the published version.

---

## Quick Start

1. Add the dependency (see [Installation](#installation--requirements)).
2. Add the minimum required configuration:

   ```yaml
   auth:
     security:
       enabled: true
       jwt-access-secret: "${JWT_ACCESS_SECRET}"
       jwt-refresh-secret: "${JWT_REFRESH_SECRET}"
   ```

3. Implement `AuthenticationProvider` to connect your user store (see [Database Connectivity](#database-connectivity-spi)).
4. Annotate any endpoints that should be publicly accessible with `@PublicApi`.
5. Start your application — every other endpoint is now authenticated by default.

---

## Configuration (`application.yml`)

Configure the library properties in your Spring Boot application:

```yaml
server:
  port: 8080

auth:
  security:
    enabled: true
    jwt-access-secret: "YOUR_SECURE_BASE64_OR_HS512_SECRET_KEY_MIN_512_BITS_LONG"
    jwt-refresh-secret: "YOUR_SECURE_BASE64_OR_HS512_SECRET_KEY_MIN_512_BITS_LONG"
    jwt-access-expiration: 86400000       # 24 Hours in milliseconds
    jwt-refresh-expiration: 604800000     # 7 Days in milliseconds

    # Whitelist custom endpoints that should bypass authentication
    public-endpoints:
      - "/hello"
      - "/api/v1/public/**"

    # Unauthenticated standard authentication endpoint paths
    api:
      login: "/api/v1/auth/login"
      register: "/api/v1/auth/register"
      refresh: "/api/v1/auth/refresh"

    cors:
      allowed-origins:
        - "*"
      allowed-methods:
        - "GET"
        - "POST"
        - "PUT"
        - "DELETE"
        - "OPTIONS"
      allowed-headers:
        - "*"
```

> ⚠️ **Security note:** Never commit real secrets to source control. Use environment variables or a secrets manager to inject `jwt-access-secret` and `jwt-refresh-secret` in production, and ensure each is at least 512 bits (64 bytes) long for HS512 signing.

---

## Usage Guide

### 1. Whitelisting Public Endpoints

Annotate controllers or methods with `@PublicApi` to bypass authentication automatically:

```java
@RestController
@PublicApi
public class PublicController {

    @GetMapping("/hello")
    public String sayHello() {
        return "Public Endpoint Access Successful";
    }
}
```

### 2. Protecting Endpoints by Role

Leverage method-level security, enabled via `@EnableMethodSecurity`:

```java
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/metrics")
    public ResponseEntity<String> getMetrics() {
        return ResponseEntity.ok("Admin Protected Data");
    }
}
```

### 3. Injecting User Context in Controllers

```java
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @GetMapping("/profile")
    public ResponseEntity<UserContext> getCurrentUser(Principal principal) {
        UserContext context = (UserContext) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        return ResponseEntity.ok(context);
    }
}
```

---

## Database Connectivity (SPI)

To connect user authentication with your persistence layer, implement the `AuthenticationProvider` interface and register it as a Spring bean:

```java
package com.oc.jwtsecurityspringbootstarter.spi;

import java.util.List;
import java.util.Optional;

public interface AuthenticationProvider {

    Optional<UserCredential> loadUserByUsername(String username);

    record UserCredential(
            String userId,
            String username,
            String email,
            String encodedPassword,
            List<String> roles
    ) {}
}
```

**Example implementation:**

```java
@Component
public class JpaAuthenticationProvider implements AuthenticationProvider {

    private final UserRepository userRepository;

    public JpaAuthenticationProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<UserCredential> loadUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(user -> new UserCredential(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getPassword(),
                        user.getRoles()
                ));
    }
}
```

The starter will automatically pick up any `AuthenticationProvider` bean present in the application context and use it during login and token validation.

---

## Security Best Practices

- **Never commit secrets.** Load `jwt-access-secret` and `jwt-refresh-secret` from environment variables, a `.env` file excluded via `.gitignore`, or a secrets manager (AWS Secrets Manager, HashiCorp Vault, Azure Key Vault, etc.).
- **Use strong keys.** Each secret must be at least 512 bits (64 bytes) for HS512 signing. Generate one with:
  ```bash
  openssl rand -base64 64
  ```
- **Rotate secrets periodically** and invalidate outstanding refresh tokens when you do.
- **Keep access token lifetimes short.** Favor short-lived access tokens (minutes to a few hours) paired with longer-lived refresh tokens, rather than the 24-hour default shown above, for higher-security applications.
- **Restrict CORS in production.** Replace the wildcard `allowed-origins: ["*"]` with your actual frontend origin(s) before deploying.
- **Serve over HTTPS only.** JWTs sent over plain HTTP can be intercepted; terminate TLS at your load balancer or gateway.
- **Store passwords hashed.** `encodedPassword` in `UserCredential` should always be a strong hash (e.g., BCrypt), never plaintext.

---

## Troubleshooting / FAQ

**My public endpoint still returns 401.**
Confirm the class or method is annotated with `@PublicApi`, or that the path is listed under `auth.security.public-endpoints`. Path patterns support Ant-style wildcards (e.g., `/api/v1/public/**`).

**I get a `WeakKeyException` on startup.**
Your `jwt-access-secret` or `jwt-refresh-secret` is shorter than 512 bits. Generate a longer key (see [Security Best Practices](#security-best-practices)).

**`@PreAuthorize` isn't being enforced.**
Make sure your application has `@EnableMethodSecurity` on a configuration class.

**How do I access the authenticated user in a service, not a controller?**
Inject `UserContext` via `SecurityContextHolder.getContext().getAuthentication().getPrincipal()`, cast to `UserContext`.

---

## Roadmap

- [ ] Publish artifacts to Maven Central
- [ ] Support for OAuth2 / social login providers
- [ ] Built-in refresh-token rotation and revocation store
- [ ] Configurable rate limiting on auth endpoints
- [ ] Reactive (WebFlux) support

Have a feature request? [Open an issue](https://github.com/shamodhas/jwt-security-spring-boot-starter/issues).

---

## Contributing

Contributions are welcome!

1. Fork the repository.
2. Create a feature branch: `git checkout -b feature/my-feature`.
3. Commit your changes with clear messages.
4. Push to your fork and open a Pull Request against `main`.

Please open an issue first for significant changes so we can discuss the approach before you invest time in an implementation.

---

## License

This project is distributed under the terms specified in [`LICENSE`](LICENSE).

---

## Repository

[github.com/shamodhas/jwt-security-spring-boot-starter](https://github.com/shamodhas/jwt-security-spring-boot-starter)