
package com.techpulse.multitenant.service;

import com.techpulse.multitenant.dto.request.LoginRequest;
import com.techpulse.multitenant.dto.response.LoginResponse;
import com.techpulse.multitenant.entity.Tenant;
import com.techpulse.multitenant.entity.User;
import com.techpulse.multitenant.enums.Role;
import com.techpulse.multitenant.repository.UserRepository;
import com.techpulse.multitenant.security.JwtService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginShouldReturnTokenForValidCredentials() {

        Tenant tenant = new Tenant();
        tenant.setId(1L);
        tenant.setName("Tenant A");

        User user = new User();
        user.setId(10L);
        user.setEmail("admin@test.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.ADMIN);
        user.setTenant(tenant);

        LoginRequest request = new LoginRequest();
        request.setEmail("admin@test.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("admin@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password123", "encodedPassword"))
                .thenReturn(true);

        when(jwtService.generateToken(
                "admin@test.com", 10L, 1L, "ADMIN"))
                .thenReturn("test-jwt-token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("test-jwt-token", response.getToken());
        assertEquals("admin@test.com", response.getEmail());
        assertEquals(10L, response.getUserId());
        assertEquals(1L, response.getTenantId());
        assertEquals(Role.ADMIN, response.getRole());

        verify(jwtService).generateToken(
                "admin@test.com", 10L, 1L, "ADMIN");
    }

    @Test
    void loginShouldRejectUnknownEmail() {

        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@test.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("unknown@test.com"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(
                anyString(), anyLong(), anyLong(), anyString());
    }

    @Test
    void loginShouldRejectIncorrectPassword() {

        Tenant tenant = new Tenant();
        tenant.setId(1L);

        User user = new User();
        user.setId(10L);
        user.setEmail("admin@test.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.ADMIN);
        user.setTenant(tenant);

        LoginRequest request = new LoginRequest();
        request.setEmail("admin@test.com");
        request.setPassword("wrongPassword");

        when(userRepository.findByEmail("admin@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("wrongPassword", "encodedPassword"))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());

        verify(jwtService, never()).generateToken(
                anyString(), anyLong(), anyLong(), anyString());
    }
}
