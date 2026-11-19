package com.techpulse.multitenant.dto.request;

import com.techpulse.multitenant.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class UpdateTaskStatusRequest {

    @NotNull(message = "Task status is required")
    private TaskStatus status;
}
