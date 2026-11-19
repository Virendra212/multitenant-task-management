
package com.techpulse.multitenant.service;

import com.techpulse.multitenant.dto.request.AssignTaskRequest;
import com.techpulse.multitenant.dto.request.TaskRequest;
import com.techpulse.multitenant.dto.request.UpdateTaskStatusRequest;
import com.techpulse.multitenant.entity.Task;
import com.techpulse.multitenant.entity.Tenant;
import com.techpulse.multitenant.entity.User;
import com.techpulse.multitenant.enums.Role;
import com.techpulse.multitenant.enums.TaskStatus;
import com.techpulse.multitenant.repository.TaskRepository;
import com.techpulse.multitenant.repository.TenantRepository;
import com.techpulse.multitenant.repository.UserRepository;
import com.techpulse.multitenant.security.UserPrincipal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private Tenant tenant;
    private User user;

    private final Long tenantId = 1L;
    private final Long userId = 10L;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(tenantId);
        tenant.setName("Tenant A");

        user = new User();
        user.setId(userId);
        user.setEmail("user@test.com");
        user.setRole(Role.USER);
        user.setTenant(tenant);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate(Role role) {
        UserPrincipal principal = new UserPrincipal(
                userId,
                tenantId,
                "user@test.com",
                role
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    @Test
    void createTaskShouldSaveTaskForAuthenticatedTenant() {

        authenticate(Role.MANAGER);

        TaskRequest request = new TaskRequest();
        request.setTitle("Test Task");
        request.setDescription("Testing task creation");
        request.setStatus(TaskStatus.values()[0]);

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(tenant));

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> {
                    Task task = invocation.getArgument(0);
                    task.setId(100L);
                    return task;
                });

        var response = taskService.createTask(request);

        assertEquals(100L, response.getId());
        assertEquals("Test Task", response.getTitle());
        assertEquals(tenantId, response.getTenantId());

        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTaskShouldFailWhenTenantDoesNotExist() {

        authenticate(Role.MANAGER);

        TaskRequest request = new TaskRequest();
        request.setTitle("Test Task");
        request.setStatus(TaskStatus.values()[0]);

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.createTask(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void assignTaskShouldAssignUserFromSameTenant() {

        authenticate(Role.MANAGER);

        Task task = new Task();
        task.setId(100L);
        task.setTitle("Test Task");
        task.setStatus(TaskStatus.values()[0]);
        task.setTenant(tenant);

        AssignTaskRequest request = new AssignTaskRequest();
        request.setUserId(userId);

        when(taskRepository.findByIdAndTenant_Id(100L, tenantId))
                .thenReturn(Optional.of(task));

        when(userRepository.findByIdAndTenant_Id(userId, tenantId))
                .thenReturn(Optional.of(user));

        when(taskRepository.save(any(Task.class)))
                .thenReturn(task);

        var response = taskService.assignTask(100L, request);

        assertEquals(userId, response.getAssignedTo());
        assertEquals(tenantId, response.getTenantId());

        verify(taskRepository).save(task);
    }

    @Test
    void assignTaskShouldRejectUserFromAnotherTenant() {

        authenticate(Role.MANAGER);

        Task task = new Task();
        task.setId(100L);
        task.setTenant(tenant);

        AssignTaskRequest request = new AssignTaskRequest();
        request.setUserId(20L);

        when(taskRepository.findByIdAndTenant_Id(100L, tenantId))
                .thenReturn(Optional.of(task));

        when(userRepository.findByIdAndTenant_Id(20L, tenantId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.assignTask(100L, request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTaskStatusShouldUpdateAssignedUsersTask() {

        authenticate(Role.USER);

        Task task = new Task();
        task.setId(100L);
        task.setTitle("Test Task");
        task.setTenant(tenant);
        task.setAssignedTo(user);
        task.setStatus(TaskStatus.values()[0]);

        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest();
        request.setStatus(TaskStatus.values()[
                TaskStatus.values().length > 1 ? 1 : 0
        ]);

        when(taskRepository.findByIdAndTenant_IdAndAssignedTo_Id(
                100L, tenantId, userId))
                .thenReturn(Optional.of(task));

        when(taskRepository.save(any(Task.class)))
                .thenReturn(task);

        var response = taskService.updateTaskStatus(100L, request);

        assertEquals(request.getStatus(), response.getStatus());
        assertEquals(userId, response.getAssignedTo());

        verify(taskRepository).save(task);
    }

    @Test
    void updateTaskStatusShouldRejectUnassignedTask() {

        authenticate(Role.USER);

        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest();
        request.setStatus(TaskStatus.values()[0]);

        when(taskRepository.findByIdAndTenant_IdAndAssignedTo_Id(
                100L, tenantId, userId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTaskStatus(100L, request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void userShouldSeeOnlyTheirAssignedTasks() {

        authenticate(Role.USER);

        Task task = new Task();
        task.setId(100L);
        task.setTitle("Assigned Task");
        task.setTenant(tenant);
        task.setAssignedTo(user);
        task.setStatus(TaskStatus.values()[0]);

        when(taskRepository.findAllByTenant_IdAndAssignedTo_Id(
                tenantId, userId))
                .thenReturn(List.of(task));

        var response = taskService.getTasks();

        assertEquals(1, response.size());
        assertEquals("Assigned Task", response.get(0).getTitle());

        verify(taskRepository)
                .findAllByTenant_IdAndAssignedTo_Id(tenantId, userId);

        verify(taskRepository, never()).findAllByTenant_Id(any());
    }

    @Test
    void managerShouldSeeAllTasksWithinTheirTenant() {

        authenticate(Role.MANAGER);

        when(taskRepository.findAllByTenant_Id(tenantId))
                .thenReturn(List.of());

        var response = taskService.getTasks();

        assertNotNull(response);

        verify(taskRepository).findAllByTenant_Id(tenantId);

        verify(taskRepository, never())
                .findAllByTenant_IdAndAssignedTo_Id(any(), any());
    }
}
