
package com.techpulse.multitenant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techpulse.multitenant.dto.request.RegisterUserRequest;
import com.techpulse.multitenant.dto.response.UserResponse;
import com.techpulse.multitenant.enums.Role;
import com.techpulse.multitenant.exception.GlobalExceptionHandler;
import com.techpulse.multitenant.service.UserService;

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
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createUserShouldReturnCreatedResponse() throws Exception {

        RegisterUserRequest request = new RegisterUserRequest();
        request.setEmail("manager@test.com");
        request.setPassword("password123");
        request.setRole(Role.MANAGER);

        UserResponse response = new UserResponse();
        response.setId(20L);
        response.setEmail("manager@test.com");
        response.setRole(Role.MANAGER);
        response.setTenantId(1L);

        when(userService.createUser(any(RegisterUserRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.email").value("manager@test.com"))
                .andExpect(jsonPath("$.role").value("MANAGER"))
                .andExpect(jsonPath("$.tenantId").value(1));

        verify(userService).createUser(any(RegisterUserRequest.class));
    }

    @Test
    void createUserShouldReturnBadRequestForInvalidEmail()
            throws Exception {

        RegisterUserRequest request = new RegisterUserRequest();
        request.setEmail("invalid-email");
        request.setPassword("password123");
        request.setRole(Role.USER);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message.email").exists());

        verify(userService, never())
                .createUser(any(RegisterUserRequest.class));
    }

    @Test
    void createUserShouldReturnBadRequestWhenPasswordIsMissing()
            throws Exception {

        RegisterUserRequest request = new RegisterUserRequest();
        request.setEmail("user@test.com");
        request.setRole(Role.USER);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message.password").exists());

        verify(userService, never())
                .createUser(any(RegisterUserRequest.class));
    }

    @Test
    void createUserShouldReturnConflictForDuplicateEmail()
            throws Exception {

        RegisterUserRequest request = new RegisterUserRequest();
        request.setEmail("existing@test.com");
        request.setPassword("password123");
        request.setRole(Role.USER);

        when(userService.createUser(any(RegisterUserRequest.class)))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Email already registered"
                ));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Email already registered"));
    }

    @Test
    void createUserShouldReturnBadRequestWhenRequestBodyIsMissing()
            throws Exception {

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isBadRequest());

        verify(userService, never())
                .createUser(any(RegisterUserRequest.class));
    }
}
