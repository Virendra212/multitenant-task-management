
package com.techpulse.multitenant.dto.response;

import com.techpulse.multitenant.enums.Role;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class LoginResponse {

    private String token;
    private String email;
    private Long userId;
    private Long tenantId;
    private Role role;
}
