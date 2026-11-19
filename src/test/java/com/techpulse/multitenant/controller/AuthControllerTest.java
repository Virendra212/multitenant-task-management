
package com.techpulse.multitenant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techpulse.multitenant.dto.request.LoginRequest;
import com.techpulse.multitenant.dto.response.LoginResponse;
import com.techpulse.multitenant.enums.Role;
import com.techpulse.multitenant.exception.GlobalExceptionHandler;
import com.techpulse.multitenant.service.AuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void loginShouldReturnSuccessForValidCredentials() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setEmail("admin@test.com");
        request.setPassword("password123");

        LoginResponse response = new LoginResponse();
        response.setToken("test-jwt-token");
        response.setEmail("admin@test.com");
        response.setUserId(10L);
        response.setTenantId(1L);
        response.setRole(Role.ADMIN);

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.email").value("admin@test.com"))
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.tenantId").value(1))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void loginShouldReturnUnauthorizedForInvalidCredentials()
            throws Exception {

        LoginRequest request = new LoginRequest();
        request.setEmail("admin@test.com");
        request.setPassword("wrongPassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message")
                        .value("Invalid email or password"));
    }

    @Test
    void loginShouldReturnBadRequestForInvalidEmail()
            throws Exception {

        LoginRequest request = new LoginRequest();
        request.setEmail("invalid-email");
        request.setPassword("password123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message.email").exists());

        verify(authService, never()).login(any(LoginRequest.class));
    }

    @Test
    void loginShouldReturnBadRequestWhenEmailIsMissing()
            throws Exception {

        LoginRequest request = new LoginRequest();
        request.setPassword("password123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.email").exists());

        verify(authService, never()).login(any(LoginRequest.class));
    }

    @Test
    void loginShouldReturnBadRequestWhenRequestBodyIsMissing()
            throws Exception {

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequest.class));
    }
}
