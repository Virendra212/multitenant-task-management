
package com.techpulse.multitenant.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class TenantResponse {

    private Long tenantId;

    private String name;

    private LocalDateTime createdAt;

}
