
package com.techpulse.multitenant.security;

import java.security.Principal;

import com.techpulse.multitenant.enums.Role;

public class UserPrincipal implements Principal {

    private final Long userId;
    private final Long tenantId;
    private final String email;
    private final Role role;

    public UserPrincipal(Long userId, Long tenantId, String email, Role role) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.email = email;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public String getName() {
        return email;
    }
}
