
package com.techpulse.multitenant.dto.request;

import com.techpulse.multitenant.enums.TaskStatus;


import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class UpdateTaskRequest {

    @Size(min = 1, max = 200,
      message = "Task title must contain between 1 and 200 characters")
        private String title;

    @Size(max = 2000, message = "Task description must not exceed 2000 characters")
    private String description;

    private TaskStatus status;

    @Positive(message = "Assigned user ID must be positive")
    private Long assignedTo;

}
