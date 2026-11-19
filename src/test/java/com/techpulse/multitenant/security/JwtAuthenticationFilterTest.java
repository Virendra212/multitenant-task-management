
package com.techpulse.multitenant.security;

import com.techpulse.multitenant.enums.Role;



import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validJwtShouldAuthenticateUser() throws Exception {

        request.addHeader("Authorization", "Bearer valid-token");

        when(jwtService.extractEmail("valid-token"))
                .thenReturn("admin@test.com");

        when(jwtService.isTokenValid("valid-token", "admin@test.com"))
                .thenReturn(true);

        when(jwtService.extractUserId("valid-token"))
                .thenReturn(10L);

        when(jwtService.extractTenantId("valid-token"))
                .thenReturn(1L);

        when(jwtService.extractRole("valid-token"))
                .thenReturn("ADMIN");

        MockFilterChain chain = new MockFilterChain();

        jwtAuthenticationFilter.doFilter(request, response, chain);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());

        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();

        assertEquals(10L, principal.getUserId());
        assertEquals(1L, principal.getTenantId());
        assertEquals("admin@test.com", principal.getEmail());
        assertEquals(Role.ADMIN, principal.getRole());

        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")));

        verify(jwtService).isTokenValid("valid-token", "admin@test.com");
    }

    @Test
    void missingJwtShouldContinueWithoutAuthentication() throws Exception {

        MockFilterChain chain = new MockFilterChain();

        jwtAuthenticationFilter.doFilter(request, response, chain);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        assertEquals(200, response.getStatus());

        verifyNoInteractions(jwtService);
    }

    @Test
    void invalidJwtShouldReturnUnauthorized() throws Exception {

        request.addHeader("Authorization", "Bearer invalid-token");

        when(jwtService.extractEmail("invalid-token"))
                .thenThrow(new RuntimeException("Invalid token"));

        MockFilterChain chain = new MockFilterChain();

        jwtAuthenticationFilter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );
    }

    @Test
    void expiredJwtShouldNotAuthenticateUser() throws Exception {

        request.addHeader("Authorization", "Bearer expired-token");

        when(jwtService.extractEmail("expired-token"))
                .thenReturn("admin@test.com");

        when(jwtService.isTokenValid("expired-token", "admin@test.com"))
                .thenReturn(false);

        MockFilterChain chain = new MockFilterChain();

        jwtAuthenticationFilter.doFilter(request, response, chain);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        assertEquals(200, response.getStatus());
    }
}
