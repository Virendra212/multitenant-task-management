
package com.techpulse.multitenant.service;

import com.techpulse.multitenant.dto.request.RegisterUserRequest;
import com.techpulse.multitenant.entity.Tenant;
import com.techpulse.multitenant.entity.User;
import com.techpulse.multitenant.enums.Role;
import com.techpulse.multitenant.repository.TenantRepository;
import com.techpulse.multitenant.repository.UserRepository;
import com.techpulse.multitenant.security.UserPrincipal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private Tenant tenant;

    private final Long tenantId = 1L;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(tenantId);
        tenant.setName("Tenant A");

        UserPrincipal principal = new UserPrincipal(
                10L,
                tenantId,
                "admin@test.com",
                Role.ADMIN
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createUserShouldSuccessfullyCreateUser() {

        RegisterUserRequest request = new RegisterUserRequest();
        request.setEmail("manager@test.com");
        request.setPassword("password123");
        request.setRole(Role.MANAGER);

        when(userRepository.existsByEmail("manager@test.com"))
                .thenReturn(false);

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(tenant));

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(20L);
                    return user;
                });

        var response = userService.createUser(request);

        assertEquals(20L, response.getId());
        assertEquals("manager@test.com", response.getEmail());
        assertEquals(Role.MANAGER, response.getRole());
        assertEquals(tenantId, response.getTenantId());

        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUserShouldRejectDuplicateEmail() {

        RegisterUserRequest request = new RegisterUserRequest();
        request.setEmail("existing@test.com");
        request.setPassword("password123");
        request.setRole(Role.USER);

        when(userRepository.existsByEmail("existing@test.com"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.createUser(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserShouldRejectAdminRole() {

        RegisterUserRequest request = new RegisterUserRequest();
        request.setEmail("newadmin@test.com");
        request.setPassword("password123");
        request.setRole(Role.ADMIN);

        when(userRepository.existsByEmail("newadmin@test.com"))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.createUser(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserShouldRejectNullRole() {

        RegisterUserRequest request = new RegisterUserRequest();
        request.setEmail("newuser@test.com");
        request.setPassword("password123");
        request.setRole(null);

        when(userRepository.existsByEmail("newuser@test.com"))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.createUser(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserShouldFailWhenTenantDoesNotExist() {

        RegisterUserRequest request = new RegisterUserRequest();
        request.setEmail("user@test.com");
        request.setPassword("password123");
        request.setRole(Role.USER);

        when(userRepository.existsByEmail("user@test.com"))
                .thenReturn(false);

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.createUser(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(userRepository, never()).save(any(User.class));
    }
}
