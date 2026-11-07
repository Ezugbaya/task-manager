package com.javarush.taskmanager.controller;

import com.javarush.taskmanager.entity.Task;
import com.javarush.taskmanager.entity.TaskPriority;
import com.javarush.taskmanager.entity.TaskStatus;
import com.javarush.taskmanager.security.JwtAuthenticationFilter;
import com.javarush.taskmanager.security.JwtService;
import com.javarush.taskmanager.service.AuthService;
import com.javarush.taskmanager.service.TaskService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UsernamePasswordAuthenticationToken testAuthentication() {
        return new UsernamePasswordAuthenticationToken(
                "testuser",
                "password"
        );
    }

    @Test
    void getAllTasksShouldReturnTaskList() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test task");
        task.setStatus(TaskStatus.NEW);
        task.setPriority(TaskPriority.MEDIUM);

        when(taskService.getAllTasks(
                eq("testuser"),
                anyString(),
                anyString()
        )).thenReturn(List.of(task));

        mockMvc.perform(get("/api/tasks")
                        .principal(testAuthentication())
                        .param("sortBy", "createdAt")
                        .param("direction", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test task"));
    }

    @Test
    void getTaskByIdShouldReturnTask() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test task");
        task.setStatus(TaskStatus.NEW);
        task.setPriority(TaskPriority.MEDIUM);

        when(taskService.getTaskById(1L, "testuser"))
                .thenReturn(task);

        mockMvc.perform(get("/api/tasks/1")
                        .principal(testAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test task"));
    }

    @Test
    void deleteTaskShouldReturnNoContent() throws Exception {
        doNothing().when(taskService).deleteTask(1L, "testuser");

        mockMvc.perform(delete("/api/tasks/1")
                        .principal(testAuthentication()))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(1L, "testuser");
    }

    @Test
    void createTaskShouldReturnCreatedTask() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("New task");
        task.setStatus(TaskStatus.NEW);
        task.setPriority(TaskPriority.MEDIUM);

        when(taskService.createTask(any(Task.class), eq("testuser")))
                .thenReturn(task);

        mockMvc.perform(post("/api/tasks")
                        .principal(testAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "New task",
                                  "description": "Description",
                                  "dueDate": "2030-01-01T12:00:00",
                                  "status": "NEW",
                                  "priority": "MEDIUM"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New task"));
    }

    @Test
    void updateTaskShouldReturnUpdatedTask() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Updated task");
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setPriority(TaskPriority.HIGH);

        when(taskService.updateTask(
                eq(1L),
                any(Task.class),
                eq("testuser")
        )).thenReturn(task);

        mockMvc.perform(put("/api/tasks/1")
                        .principal(testAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated task",
                                  "description": "Updated description",
                                  "dueDate": "2030-01-02T12:00:00",
                                  "status": "IN_PROGRESS",
                                  "priority": "HIGH"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated task"));
    }

    @Test
    void getStatisticsShouldReturnCounters() throws Exception {
        mockMvc.perform(get("/api/tasks/statistics")
                        .principal(testAuthentication()))
                .andExpect(status().isOk());
    }
}