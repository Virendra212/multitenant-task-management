
package com.techpulse.multitenant.service;

import com.techpulse.multitenant.dto.request.CreateTenantRequest;
import com.techpulse.multitenant.dto.response.TenantResponse;
import com.techpulse.multitenant.entity.Tenant;
import com.techpulse.multitenant.entity.User;
import com.techpulse.multitenant.enums.Role;
import com.techpulse.multitenant.repository.TenantRepository;
import com.techpulse.multitenant.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TenantService tenantService;

    @Test
    void createTenantShouldSuccessfullyRegisterTenantAndAdmin() {

        CreateTenantRequest request = new CreateTenantRequest();
        request.setName("Tenant A");
        request.setAdminEmail("admin@tenantA.com");
        request.setAdminPassword("password123");

        when(userRepository.existsByEmail("admin@tenantA.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(tenantRepository.save(any(Tenant.class)))
                .thenAnswer(invocation -> {
                    Tenant tenant = invocation.getArgument(0);
                    tenant.setId(1L);
                    tenant.setCreatedAt(LocalDateTime.now());
                    return tenant;
                });

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TenantResponse response = tenantService.createTenant(request);

        assertNotNull(response);
        assertEquals(1L, response.getTenantId());
        assertEquals("Tenant A", response.getName());
        assertNotNull(response.getCreatedAt());

        verify(tenantRepository).save(any(Tenant.class));
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void createTenantShouldCreateFirstUserWithAdminRole() {

        CreateTenantRequest request = new CreateTenantRequest();
        request.setName("Tenant B");
        request.setAdminEmail("admin@tenantB.com");
        request.setAdminPassword("password123");

        when(userRepository.existsByEmail("admin@tenantB.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(tenantRepository.save(any(Tenant.class)))
                .thenAnswer(invocation -> {
                    Tenant tenant = invocation.getArgument(0);
                    tenant.setId(2L);
                    return tenant;
                });

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User admin = invocation.getArgument(0);

                    assertEquals(Role.ADMIN, admin.getRole());
                    assertEquals("admin@tenantB.com", admin.getEmail());
                    assertEquals("encodedPassword", admin.getPassword());
                    assertEquals(2L, admin.getTenant().getId());

                    return admin;
                });

        tenantService.createTenant(request);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void createTenantShouldEncodeAdminPassword() {

        CreateTenantRequest request = new CreateTenantRequest();
        request.setName("Tenant C");
        request.setAdminEmail("admin@tenantC.com");
        request.setAdminPassword("password123");

        when(userRepository.existsByEmail("admin@tenantC.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("secureEncodedPassword");

        when(tenantRepository.save(any(Tenant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        tenantService.createTenant(request);

        verify(passwordEncoder).encode("password123");

        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("secureEncodedPassword")
        ));
    }

    @Test
    void createTenantShouldRejectDuplicateAdminEmail() {

        CreateTenantRequest request = new CreateTenantRequest();
        request.setName("Tenant D");
        request.setAdminEmail("existing@test.com");
        request.setAdminPassword("password123");

        when(userRepository.existsByEmail("existing@test.com"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> tenantService.createTenant(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(tenantRepository, never()).save(any(Tenant.class));
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }
}
