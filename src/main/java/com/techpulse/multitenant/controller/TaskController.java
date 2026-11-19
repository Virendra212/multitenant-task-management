package com.techpulse.multitenant.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.techpulse.multitenant.dto.request.TaskRequest;
import com.techpulse.multitenant.dto.request.UpdateTaskStatusRequest;
import com.techpulse.multitenant.dto.request.AssignTaskRequest;
import com.techpulse.multitenant.dto.response.TaskResponse;
import com.techpulse.multitenant.service.TaskService;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // MANAGER creates a task
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskRequest request) {

        return new ResponseEntity<>(
                taskService.createTask(request),
                HttpStatus.CREATED
        );
    }

    // MANAGER assigns a task to a USER
    @PutMapping("/{id}/assign")
    public ResponseEntity<TaskResponse> assignTask(
            @PathVariable Long id,
            @Valid @RequestBody AssignTaskRequest request) {

        return ResponseEntity.ok(
                taskService.assignTask(id, request)
        );
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskStatusRequest request) {

        return ResponseEntity.ok(
                taskService.updateTaskStatus(id, request)
        );
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks() {
        return ResponseEntity.ok(taskService.getTasks());
    }
}
