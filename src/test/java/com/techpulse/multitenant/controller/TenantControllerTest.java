
package com.techpulse.multitenant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techpulse.multitenant.dto.request.CreateTenantRequest;
import com.techpulse.multitenant.dto.response.TenantResponse;
import com.techpulse.multitenant.exception.GlobalExceptionHandler;
import com.techpulse.multitenant.service.TenantService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TenantControllerTest {

    @Mock
    private TenantService tenantService;

    @InjectMocks
    private TenantController tenantController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders
                .standaloneSetup(tenantController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void registerTenantShouldReturnCreatedResponse() throws Exception {

        CreateTenantRequest request = new CreateTenantRequest();
        request.setName("Tenant A");
        request.setAdminEmail("admin@tenantA.com");
        request.setAdminPassword("password123");

        TenantResponse response = new TenantResponse();
        response.setTenantId(1L);
        response.setName("Tenant A");
        response.setCreatedAt(LocalDateTime.now());

        when(tenantService.createTenant(any(CreateTenantRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/tenant/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenantId").value(1))
                .andExpect(jsonPath("$.name").value("Tenant A"))
                .andExpect(jsonPath("$.createdAt").exists());

        verify(tenantService).createTenant(any(CreateTenantRequest.class));
    }

    @Test
    void registerTenantShouldReturnBadRequestForInvalidInput()
            throws Exception {

        CreateTenantRequest request = new CreateTenantRequest();

        request.setName("");
        request.setAdminEmail("invalid-email");
        request.setAdminPassword("123");

        mockMvc.perform(post("/tenant/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message.name").exists())
                .andExpect(jsonPath("$.message.adminEmail").exists())
                .andExpect(jsonPath("$.message.adminPassword").exists());

        verify(tenantService, never())
                .createTenant(any(CreateTenantRequest.class));
    }

    @Test
    void registerTenantShouldReturnConflictForDuplicateEmail()
            throws Exception {

        CreateTenantRequest request = new CreateTenantRequest();

        request.setName("Tenant B");
        request.setAdminEmail("existing@test.com");
        request.setAdminPassword("password123");

        when(tenantService.createTenant(any(CreateTenantRequest.class)))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Email is already registered"
                ));

        mockMvc.perform(post("/tenant/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Email is already registered"));
    }

    @Test
    void registerTenantShouldRejectMissingRequestBody()
            throws Exception {

        mockMvc.perform(post("/tenant/register")
                .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isBadRequest());

        verify(tenantService, never())
                .createTenant(any(CreateTenantRequest.class));
    }
}
