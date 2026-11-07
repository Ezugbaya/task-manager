package com.javarush.taskmanager.controller;

import com.javarush.taskmanager.dto.TaskRequest;
import com.javarush.taskmanager.dto.TaskStatisticsResponse;
import com.javarush.taskmanager.entity.Task;
import com.javarush.taskmanager.entity.TaskPriority;
import com.javarush.taskmanager.entity.TaskStatus;
import com.javarush.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<Task> getAllTasks(
            Authentication authentication,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        return taskService.getAllTasks(
                authentication.getName(),
                sortBy,
                direction
        );
    }

    @GetMapping("/{id}")
    public Task getTaskById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return taskService.getTaskById(id, authentication.getName());
    }

    @PostMapping
    public Task createTask(
            @Valid @RequestBody TaskRequest request,
            Authentication authentication
    ) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());

        return taskService.createTask(task, authentication.getName());
    }

    @PutMapping("/{id}")
    public Task updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request,
            Authentication authentication
    ) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());

        return taskService.updateTask(
                id,
                task,
                authentication.getName()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            Authentication authentication
    ) {
        taskService.deleteTask(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    public List<Task> getTasksByStatus(
            @PathVariable TaskStatus status,
            Authentication authentication
    ) {
        return taskService.getTasksByStatus(
                status,
                authentication.getName()
        );
    }

    @GetMapping("/overdue")
    public List<Task> getOverdueTasks(Authentication authentication) {
        return taskService.getOverdueTasks(authentication.getName());
    }

    @GetMapping("/statistics")
    public TaskStatisticsResponse getTaskStatistics(
            Authentication authentication
    ) {
        return taskService.getTaskStatistics(authentication.getName());
    }
}
