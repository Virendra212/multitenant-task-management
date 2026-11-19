
package com.techpulse.multitenant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.techpulse.multitenant.dto.request.TaskRequest;
import com.techpulse.multitenant.dto.request.AssignTaskRequest;
import com.techpulse.multitenant.dto.request.UpdateTaskStatusRequest;
import com.techpulse.multitenant.dto.response.TaskResponse;
import com.techpulse.multitenant.enums.TaskStatus;
import com.techpulse.multitenant.exception.GlobalExceptionHandler;
import com.techpulse.multitenant.service.TaskService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders
                .standaloneSetup(taskController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createTaskShouldReturnCreatedResponse() throws Exception {

        TaskRequest request = new TaskRequest();
        request.setTitle("Test Task");
        request.setDescription("Testing task creation");
        request.setStatus(TaskStatus.values()[0]);

        TaskResponse response = new TaskResponse();
        response.setId(100L);
        response.setTitle("Test Task");
        response.setDescription("Testing task creation");
        response.setStatus(request.getStatus());
        response.setTenantId(1L);

        when(taskService.createTask(any(TaskRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.tenantId").value(1));

        verify(taskService).createTask(any(TaskRequest.class));
    }

    @Test
    void assignTaskShouldReturnSuccessResponse() throws Exception {

        AssignTaskRequest request = new AssignTaskRequest();
        request.setUserId(10L);

        TaskResponse response = new TaskResponse();
        response.setId(100L);
        response.setTitle("Test Task");
        response.setAssignedTo(10L);
        response.setTenantId(1L);

        when(taskService.assignTask(eq(100L), any(AssignTaskRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/tasks/100/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.assignedTo").value(10));

        verify(taskService).assignTask(
                eq(100L), any(AssignTaskRequest.class));
    }

    @Test
    void updateTaskStatusShouldReturnSuccessResponse() throws Exception {

        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest();
        request.setStatus(TaskStatus.values()[0]);

        TaskResponse response = new TaskResponse();
        response.setId(100L);
        response.setTitle("Test Task");
        response.setStatus(request.getStatus());
        response.setAssignedTo(10L);
        response.setTenantId(1L);

        when(taskService.updateTaskStatus(
                eq(100L), any(UpdateTaskStatusRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/tasks/100/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.assignedTo").value(10));

        verify(taskService).updateTaskStatus(
                eq(100L), any(UpdateTaskStatusRequest.class));
    }

    @Test
    void getTasksShouldReturnTaskList() throws Exception {

        TaskResponse task = new TaskResponse();
        task.setId(100L);
        task.setTitle("Test Task");
        task.setStatus(TaskStatus.values()[0]);
        task.setTenantId(1L);

        when(taskService.getTasks())
                .thenReturn(List.of(task));

        mockMvc.perform(get("/tasks"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].title").value("Test Task"));

        verify(taskService).getTasks();
    }

    @Test
    void createTaskShouldReturnBadRequestForInvalidTitle()
            throws Exception {

        TaskRequest request = new TaskRequest();
        request.setTitle("");
        request.setDescription("Invalid task");
        request.setStatus(TaskStatus.values()[0]);

        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message.title").exists());

        verify(taskService, never()).createTask(any(TaskRequest.class));
    }

    @Test
    void createTaskShouldReturnBadRequestWhenBodyIsMissing()
            throws Exception {

        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isBadRequest());

        verify(taskService, never()).createTask(any(TaskRequest.class));
    }

    @Test
    void assignTaskShouldReturnNotFoundWhenTaskDoesNotExist()
            throws Exception {

        AssignTaskRequest request = new AssignTaskRequest();
        request.setUserId(10L);

        when(taskService.assignTask(eq(999L), any(AssignTaskRequest.class)))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Task not found"
                ));

        mockMvc.perform(put("/tasks/999/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found"));
    }
}
