
package com.techpulse.multitenant.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class CreateTenantRequest {

    @NotBlank(message = "Tenant name is required")
    @Size(max = 150, message = "Tenant name must not exceed 150 characters")
    private String name;

    @NotBlank(message = "Admin email is required")
    @Email(message = "Admin email must be valid")
    @Size(max = 150, message = "Admin email must not exceed 150 characters")
    private String adminEmail;

    @NotBlank(message = "Admin password is required")
    @Size(min = 8, message = "Admin password must contain at least 8 characters")
    private String adminPassword;
}
