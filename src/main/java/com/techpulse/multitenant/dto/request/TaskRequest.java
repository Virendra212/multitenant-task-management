
package com.techpulse.multitenant.dto.request;

import com.techpulse.multitenant.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class TaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(max = 200, message = "Task title must not exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Task description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Task status is required")
    private TaskStatus status;
}
