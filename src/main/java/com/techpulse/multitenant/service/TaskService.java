
package com.techpulse.multitenant.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.techpulse.multitenant.dto.request.AssignTaskRequest;
import com.techpulse.multitenant.dto.request.TaskRequest;
import com.techpulse.multitenant.dto.request.UpdateTaskStatusRequest;
import com.techpulse.multitenant.dto.response.TaskResponse;
import com.techpulse.multitenant.entity.Task;
import com.techpulse.multitenant.repository.TaskRepository;
import com.techpulse.multitenant.repository.TenantRepository;
import com.techpulse.multitenant.repository.UserRepository;
import com.techpulse.multitenant.security.UserPrincipal;


import com.techpulse.multitenant.entity.User;
import com.techpulse.multitenant.enums.Role;

import lombok.RequiredArgsConstructor;

import java.util.List;


@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    public TaskResponse createTask(TaskRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();

        Long tenantId = principal.getTenantId();

        var tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Tenant not found"));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setTenant(tenant);

        Task savedTask = taskRepository.save(task);

        TaskResponse response = new TaskResponse();
        response.setId(savedTask.getId());
        response.setTitle(savedTask.getTitle());
        response.setDescription(savedTask.getDescription());
        response.setStatus(savedTask.getStatus());
        response.setTenantId(tenantId);

        return response;
    }


    public TaskResponse assignTask(Long taskId, AssignTaskRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();

        Long tenantId = principal.getTenantId();

        Task task = taskRepository.findByIdAndTenant_Id(taskId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task not found"));

        User user = userRepository.findByIdAndTenant_Id(
                        request.getUserId(), tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found in your tenant"));

        if (user.getRole() != Role.USER) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tasks can only be assigned to USER accounts");
        }

        task.setAssignedTo(user);

        Task savedTask = taskRepository.save(task);

        TaskResponse response = new TaskResponse();
        response.setId(savedTask.getId());
        response.setTitle(savedTask.getTitle());
        response.setDescription(savedTask.getDescription());
        response.setStatus(savedTask.getStatus());
        response.setAssignedTo(user.getId());
        response.setTenantId(tenantId);

        return response;
    }


        public TaskResponse updateTaskStatus(
            Long taskId,
            UpdateTaskStatusRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();

        Long tenantId = principal.getTenantId();
        Long userId = principal.getUserId();

        Task task = taskRepository
                .findByIdAndTenant_IdAndAssignedTo_Id(
                        taskId, tenantId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Task not found or not assigned to you"));

        task.setStatus(request.getStatus());

        Task savedTask = taskRepository.save(task);

        TaskResponse response = new TaskResponse();
        response.setId(savedTask.getId());
        response.setTitle(savedTask.getTitle());
        response.setDescription(savedTask.getDescription());
        response.setStatus(savedTask.getStatus());
        response.setAssignedTo(userId);
        response.setTenantId(tenantId);

        return response;
    }

    public List<TaskResponse> getTasks() {

    Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

    UserPrincipal principal =
            (UserPrincipal) authentication.getPrincipal();

    Long tenantId = principal.getTenantId();
    Long userId = principal.getUserId();
    Role role = principal.getRole();

    List<Task> tasks;

    if (role == Role.USER) {
        tasks = taskRepository
                .findAllByTenant_IdAndAssignedTo_Id(tenantId, userId);
    } else {
        tasks = taskRepository.findAllByTenant_Id(tenantId);
    }

    return tasks.stream().map(task -> {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus());
        response.setTenantId(tenantId);

        if (task.getAssignedTo() != null) {
            response.setAssignedTo(task.getAssignedTo().getId());
        }

        return response;
    }).toList();
}
}
