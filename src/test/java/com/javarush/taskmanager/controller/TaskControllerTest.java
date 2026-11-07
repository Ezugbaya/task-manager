package com.javarush.taskmanager.controller;

import com.javarush.taskmanager.entity.Task;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
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

    @Test
    void getAllTasksShouldReturnTaskList() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Тестовая задача");
        task.setDescription("Описание задачи");
        task.setStatus(TaskStatus.NEW);

        when(taskService.getAllTasks()).thenReturn(List.of(task));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Тестовая задача"))
                .andExpect(jsonPath("$[0].status").value("NEW"));
    }

    @Test
    void getTaskByIdShouldReturnTask() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Тестовая задача");
        task.setDescription("Описание задачи");
        task.setStatus(TaskStatus.NEW);

        when(taskService.getTaskById(1L)).thenReturn(task);

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Тестовая задача"))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    void deleteTaskShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(1L);
    }

    @Test
    void createTaskShouldReturnCreatedTask() throws Exception {
        Task createdTask = new Task();
        createdTask.setId(1L);
        createdTask.setTitle("Новая задача");
        createdTask.setDescription("Описание новой задачи");
        createdTask.setStatus(TaskStatus.NEW);

        when(taskService.createTask(any(Task.class))).thenReturn(createdTask);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Новая задача",
                                    "description": "Описание новой задачи",
                                    "status": "NEW"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Новая задача"))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    void updateTaskShouldReturnUpdatedTask() throws Exception {
        Task existingTask = new Task();
        existingTask.setId(1L);
        existingTask.setTitle("Старая задача");
        existingTask.setDescription("Старое описание");
        existingTask.setStatus(TaskStatus.NEW);

        Task updatedTask = new Task();
        updatedTask.setId(1L);
        updatedTask.setTitle("Обновлённая задача");
        updatedTask.setDescription("Новое описание");
        updatedTask.setStatus(TaskStatus.IN_PROGRESS);

        when(taskService.getTaskById(1L)).thenReturn(existingTask);
        when(taskService.updateTask(eq(1L), any(Task.class))).thenReturn(updatedTask);

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "title": "Обновлённая задача",
                                "description": "Новое описание",
                                "status": "IN_PROGRESS"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Обновлённая задача"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }
}