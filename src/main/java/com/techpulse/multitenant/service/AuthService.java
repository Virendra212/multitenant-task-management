
package com.techpulse.multitenant.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.techpulse.multitenant.dto.request.LoginRequest;
import com.techpulse.multitenant.dto.response.LoginResponse;
import com.techpulse.multitenant.entity.User;
import com.techpulse.multitenant.repository.UserRepository;
import com.techpulse.multitenant.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {

        // 1. Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        // 2. Verify password
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        // 3. Generate JWT
        String token = jwtService.generateToken(
                user.getEmail(),
                user.getId(),
                user.getTenant().getId(),
                user.getRole().name()
        );

        // 4. Return response
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setEmail(user.getEmail());
        response.setUserId(user.getId());
        response.setTenantId(user.getTenant().getId());
        response.setRole(user.getRole());

        return response;
    }
}
