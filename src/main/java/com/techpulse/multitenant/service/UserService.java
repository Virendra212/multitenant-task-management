
package com.techpulse.multitenant.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.techpulse.multitenant.dto.request.RegisterUserRequest;
import com.techpulse.multitenant.dto.response.UserResponse;
import com.techpulse.multitenant.entity.User;
import com.techpulse.multitenant.enums.Role;
import com.techpulse.multitenant.repository.UserRepository;
import com.techpulse.multitenant.repository.TenantRepository;
import com.techpulse.multitenant.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse createUser(RegisterUserRequest request) {

        // 1. Check whether email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already registered"
            );
        }

        // 2. Validate requested role
        if (request.getRole() == null || request.getRole() == Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Role must be MANAGER or USER"
            );
        }

        // 3. Get authenticated ADMIN from SecurityContext
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();

        // 4. Get tenant ID from ADMIN's JWT principal
        Long tenantId = principal.getTenantId();

        // 5. Find the ADMIN's tenant
        var tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Tenant not found"
                ));

        // 6. Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setTenant(tenant);

        // 7. Save user
        User savedUser = userRepository.save(user);

        // 8. Prepare response
        UserResponse response = new UserResponse();
        response.setId(savedUser.getId());
        response.setEmail(savedUser.getEmail());
        response.setRole(savedUser.getRole());
        response.setTenantId(tenantId);

        return response;
    }
}
