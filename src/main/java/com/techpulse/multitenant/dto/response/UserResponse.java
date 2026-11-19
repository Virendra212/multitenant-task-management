
package com.techpulse.multitenant.dto.response;

import com.techpulse.multitenant.enums.Role;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class UserResponse {

    private Long id;

    private String email;

    private Role role;

    private Long tenantId;

}
