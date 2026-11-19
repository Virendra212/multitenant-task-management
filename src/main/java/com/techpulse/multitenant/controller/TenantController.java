
package com.techpulse.multitenant.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.techpulse.multitenant.dto.request.CreateTenantRequest;
import com.techpulse.multitenant.dto.response.TenantResponse;
import com.techpulse.multitenant.service.TenantService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tenant")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @PostMapping("/register")
    public ResponseEntity<TenantResponse> registerTenant(
            @Valid @RequestBody CreateTenantRequest request) {

        TenantResponse response =
                tenantService.createTenant(request);

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED
        );
    }
}
