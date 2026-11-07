package com.javarush.taskmanager.service;

import com.javarush.taskmanager.entity.Task;
import com.javarush.taskmanager.entity.TaskStatus;
import com.javarush.taskmanager.exception.NotFoundException;
import com.javarush.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task task;

    @BeforeEach
    void setUp() {
        task = new Task();
        task.setId(1L);
        task.setTitle("Тестовая задача");
        task.setDescription("Описание задачи");
        task.setStatus(TaskStatus.NEW);
    }

    @Test
    void getTaskByIdShouldReturnTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Task result = taskService.getTaskById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Тестовая задача", result.getTitle());

        verify(taskRepository).findById(1L);
    }

    @Test
    void getTaskByIdShouldThrowNotFoundException() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> taskService.getTaskById(999L)
        );

        verify(taskRepository).findById(999L);
    }

    @Test
    void createTaskShouldSaveTask() {
        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.createTask(task);

        assertNotNull(result);
        assertEquals("Тестовая задача", result.getTitle());

        verify(taskRepository).save(task);
    }

    @Test
    void updateTaskShouldUpdateExistingTask() {
        Task updatedTask = new Task();
        updatedTask.setTitle("Обновлённая задача");
        updatedTask.setDescription("Новое описание");
        updatedTask.setStatus(TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.updateTask(1L, updatedTask);

        assertEquals("Обновлённая задача", result.getTitle());
        assertEquals("Новое описание", result.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());

        verify(taskRepository).findById(1L);
        verify(taskRepository).save(task);
    }

    @Test
    void deleteTaskShouldDeleteExistingTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.deleteTask(1L);

        verify(taskRepository).findById(1L);
        verify(taskRepository).delete(task);
    }

    @Test
    void getAllTasksShouldReturnTaskList() {
        when(taskRepository.findAll()).thenReturn(java.util.List.of(task));

        var result = taskService.getAllTasks();

        assertEquals(1, result.size());
        assertEquals("Тестовая задача", result.get(0).getTitle());

        verify(taskRepository).findAll();
    }

    @Test
    void getTasksByStatusShouldReturnMatchingTasks() {
        when(taskRepository.findByStatus(TaskStatus.NEW))
                .thenReturn(java.util.List.of(task));

        var result = taskService.getTasksByStatus(TaskStatus.NEW);

        assertEquals(1, result.size());
        assertEquals(TaskStatus.NEW, result.get(0).getStatus());

        verify(taskRepository).findByStatus(TaskStatus.NEW);
    }

    @Test
    void getOverdueTasksShouldReturnTasksWithPastDueDate() {
        when(taskRepository.findByDueDateBefore(any(java.time.LocalDateTime.class)))
                .thenReturn(java.util.List.of(task));

        var result = taskService.getOverdueTasks();

        assertEquals(1, result.size());
        verify(taskRepository).findByDueDateBefore(any(java.time.LocalDateTime.class));
    }

    @Test
    void getTaskStatisticsShouldReturnCountsForAllStatuses() {
        when(taskRepository.countByStatus(TaskStatus.NEW)).thenReturn(3L);
        when(taskRepository.countByStatus(TaskStatus.IN_PROGRESS)).thenReturn(2L);
        when(taskRepository.countByStatus(TaskStatus.COMPLETED)).thenReturn(5L);
        when(taskRepository.countByStatus(TaskStatus.CANCELLED)).thenReturn(1L);

        var result = taskService.getTaskStatistics();

        assertEquals(4, result.size());
        assertEquals(3L, result.get(TaskStatus.NEW));
        assertEquals(2L, result.get(TaskStatus.IN_PROGRESS));
        assertEquals(5L, result.get(TaskStatus.COMPLETED));
        assertEquals(1L, result.get(TaskStatus.CANCELLED));

        verify(taskRepository, times(4)).countByStatus(any(TaskStatus.class));
    }
}