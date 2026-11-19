
package com.techpulse.multitenant.repository;

import com.techpulse.multitenant.entity.Tenant;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository
        extends JpaRepository<Tenant, Long> {

}
