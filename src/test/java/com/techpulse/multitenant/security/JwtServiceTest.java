
package com.techpulse.multitenant.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    private final String secret =
            "nY2e5vfUoGVkLLrAiV4GgWQ/CL1l+VSri3+bL8q+Y4o=";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "secret", secret);
        ReflectionTestUtils.setField(jwtService, "expiration", 3600000L);
    }

    @Test
    void generateTokenShouldCreateValidJwt() {

        String token = jwtService.generateToken(
                "admin@test.com",
                10L,
                1L,
                "ADMIN"
        );

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);

        assertTrue(jwtService.isTokenValid(token, "admin@test.com"));
    }

    @Test
    void extractEmailShouldReturnCorrectEmail() {

        String token = jwtService.generateToken(
                "admin@test.com", 10L, 1L, "ADMIN"
        );

        assertEquals("admin@test.com",
                jwtService.extractEmail(token));
    }

    @Test
    void extractUserIdShouldReturnCorrectUserId() {

        String token = jwtService.generateToken(
                "admin@test.com", 10L, 1L, "ADMIN"
        );

        assertEquals(10L, jwtService.extractUserId(token));
    }

    @Test
    void extractTenantIdShouldReturnCorrectTenantId() {

        String token = jwtService.generateToken(
                "admin@test.com", 10L, 1L, "ADMIN"
        );

        assertEquals(1L, jwtService.extractTenantId(token));
    }

    @Test
    void extractRoleShouldReturnCorrectRole() {

        String token = jwtService.generateToken(
                "admin@test.com", 10L, 1L, "ADMIN"
        );

        assertEquals("ADMIN", jwtService.extractRole(token));
    }

    @Test
    void isTokenValidShouldRejectDifferentEmail() {

        String token = jwtService.generateToken(
                "admin@test.com", 10L, 1L, "ADMIN"
        );

        assertFalse(jwtService.isTokenValid(
                token, "another@test.com"));
    }

    @Test
    void isTokenValidShouldRejectInvalidToken() {

        assertFalse(jwtService.isTokenValid(
                "invalid.jwt.token", "admin@test.com"));
    }
}
