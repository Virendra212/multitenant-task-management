
package com.techpulse.multitenant.repository;

import com.techpulse.multitenant.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long> {

    // Used during login to identify the user
    Optional<User> findByEmail(String email);

    // Check whether an email already exists
    boolean existsByEmail(String email);

    // Find a user only within a particular tenant
    Optional<User> findByIdAndTenant_Id(
            Long userId,
            Long tenantId
    );

    // Find a user using email and tenant
    Optional<User> findByEmailAndTenant_Id(
            String email,
            Long tenantId
    );

}
