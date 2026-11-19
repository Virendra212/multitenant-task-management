
package com.techpulse.multitenant.service;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.techpulse.multitenant.dto.request.CreateTenantRequest;
import com.techpulse.multitenant.dto.response.TenantResponse;
import com.techpulse.multitenant.entity.Tenant;
import com.techpulse.multitenant.entity.User;
import com.techpulse.multitenant.enums.Role;
import com.techpulse.multitenant.repository.TenantRepository;
import com.techpulse.multitenant.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;



@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public TenantResponse createTenant(CreateTenantRequest request) {


        if (userRepository.existsByEmail(request.getAdminEmail())) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered"
                );
        }


        // Step 1: Create tenant
        Tenant tenant = new Tenant();
        tenant.setName(request.getName());

        // Step 2: Save tenant
        Tenant savedTenant = tenantRepository.save(tenant);

        // Step 3: Create first admin user
        User admin = new User();
        admin.setEmail(request.getAdminEmail());
        admin.setPassword(
            passwordEncoder.encode(request.getAdminPassword())
        );
        admin.setRole(Role.ADMIN);
        admin.setTenant(savedTenant);

        // Step 4: Save admin
        userRepository.save(admin);

        // Step 5: Prepare response
        TenantResponse response = new TenantResponse();
        response.setTenantId(savedTenant.getId());
        response.setName(savedTenant.getName());
        response.setCreatedAt(savedTenant.getCreatedAt());

        return response;
    }
}
