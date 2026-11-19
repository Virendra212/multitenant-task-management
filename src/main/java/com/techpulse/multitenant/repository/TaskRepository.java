
package com.techpulse.multitenant.repository;

import com.techpulse.multitenant.entity.Task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository
        extends JpaRepository<Task, Long> {

    // Fetch all tasks belonging to a tenant
    List<Task> findAllByTenant_Id(Long tenantId);

    // Fetch tasks assigned to a particular user
    List<Task> findAllByTenant_IdAndAssignedTo_Id(
            Long tenantId,
            Long userId
    );

    // Find a task belonging to a particular tenant
    Optional<Task> findByIdAndTenant_Id(
            Long taskId,
            Long tenantId
    );

    // Find a task assigned to a particular user
    Optional<Task> findByIdAndTenant_IdAndAssignedTo_Id(
            Long taskId,
            Long tenantId,
            Long userId
    );

}
