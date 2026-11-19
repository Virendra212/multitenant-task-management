
package com.techpulse.multitenant.dto.response;

import com.techpulse.multitenant.enums.TaskStatus;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class TaskResponse {

    private Long id;

    private String title;

    private String description;

    private TaskStatus status;

    private Long assignedTo;

    private Long tenantId;

}
