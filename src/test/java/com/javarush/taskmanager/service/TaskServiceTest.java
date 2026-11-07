package com.javarush.taskmanager.service;

import com.javarush.taskmanager.dto.TaskStatisticsResponse;
import com.javarush.taskmanager.entity.Task;
import com.javarush.taskmanager.entity.TaskPriority;
import com.javarush.taskmanager.entity.TaskStatus;
import com.javarush.taskmanager.entity.User;
import com.javarush.taskmanager.exception.NotFoundException;
import com.javarush.taskmanager.repository.TaskRepository;
import com.javarush.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private static final String USERNAME = "testuser";

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername(USERNAME);

        task = new Task();
        task.setId(1L);
        task.setTitle("Тестовая задача");
        task.setDescription("Описание задачи");
        task.setStatus(TaskStatus.NEW);
        task.setPriority(TaskPriority.MEDIUM);
        task.setOwner(user);
    }

    @Test
    void getTaskByIdShouldReturnTaskForOwner() {
        when(taskRepository.findByIdAndOwnerUsername(1L, USERNAME))
                .thenReturn(Optional.of(task));

        Task result = taskService.getTaskById(1L, USERNAME);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Тестовая задача", result.getTitle());

        verify(taskRepository).findByIdAndOwnerUsername(1L, USERNAME);
    }

    @Test
    void getTaskByIdShouldThrowWhenTaskDoesNotBelongToOwner() {
        when(taskRepository.findByIdAndOwnerUsername(999L, USERNAME))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> taskService.getTaskById(999L, USERNAME)
        );

        verify(taskRepository).findByIdAndOwnerUsername(999L, USERNAME);
    }

    @Test
    void createTaskShouldAssignOwnerAndSaveTask() {
        when(userRepository.findByUsername(USERNAME))
                .thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Task newTask = new Task();
        newTask.setTitle("Новая задача");
        newTask.setDescription("Описание");

        Task result = taskService.createTask(newTask, USERNAME);

        assertNotNull(result);
        assertEquals(USERNAME, result.getOwner().getUsername());
        assertEquals(TaskStatus.NEW, result.getStatus());
        assertEquals(TaskPriority.MEDIUM, result.getPriority());

        verify(userRepository).findByUsername(USERNAME);
        verify(taskRepository).save(newTask);
    }

    @Test
    void createTaskShouldThrowWhenUserDoesNotExist() {
        when(userRepository.findByUsername(USERNAME))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> taskService.createTask(new Task(), USERNAME)
        );

        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTaskShouldUpdateOnlyOwnersTask() {
        Task updatedTask = new Task();
        updatedTask.setTitle("Обновлённая задача");
        updatedTask.setDescription("Новое описание");
        updatedTask.setStatus(TaskStatus.IN_PROGRESS);
        updatedTask.setPriority(TaskPriority.HIGH);

        when(taskRepository.findByIdAndOwnerUsername(1L, USERNAME))
                .thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.updateTask(1L, updatedTask, USERNAME);

        assertEquals("Обновлённая задача", result.getTitle());
        assertEquals("Новое описание", result.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        assertEquals(TaskPriority.HIGH, result.getPriority());

        verify(taskRepository).save(task);
    }

    @Test
    void deleteTaskShouldDeleteOwnersTask() {
        when(taskRepository.findByIdAndOwnerUsername(1L, USERNAME))
                .thenReturn(Optional.of(task));

        taskService.deleteTask(1L, USERNAME);

        verify(taskRepository).delete(task);
    }

    @Test
    void getAllTasksShouldReturnSortedTasks() {
        when(taskRepository.findAllByOwnerUsername(
                eq(USERNAME), any(Sort.class)))
                .thenReturn(List.of(task));

        List<Task> result = taskService.getAllTasks(
                USERNAME, "createdAt", "DESC");

        assertEquals(1, result.size());
        assertEquals("Тестовая задача", result.get(0).getTitle());

        verify(taskRepository).findAllByOwnerUsername(
                eq(USERNAME), any(Sort.class));
    }

    @Test
    void getAllTasksShouldRejectInvalidSortField() {
        assertThrows(
                IllegalArgumentException.class,
                () -> taskService.getAllTasks(USERNAME, "password", "ASC")
        );

        verifyNoInteractions(taskRepository);
    }

    @Test
    void getAllTasksShouldRejectInvalidSortDirection() {
        assertThrows(
                IllegalArgumentException.class,
                () -> taskService.getAllTasks(USERNAME, "createdAt", "WRONG")
        );

        verifyNoInteractions(taskRepository);
    }

    @Test
    void getTasksByStatusShouldReturnOnlyOwnersTasks() {
        when(taskRepository.findAllByOwnerUsernameAndStatus(
                USERNAME, TaskStatus.NEW))
                .thenReturn(List.of(task));

        List<Task> result = taskService.getTasksByStatus(
                TaskStatus.NEW, USERNAME);

        assertEquals(1, result.size());
        assertEquals(TaskStatus.NEW, result.get(0).getStatus());

        verify(taskRepository).findAllByOwnerUsernameAndStatus(
                USERNAME, TaskStatus.NEW);
    }

    @Test
    void getOverdueTasksShouldExcludeCompletedAndCancelled() {
        when(taskRepository.findAllByOwnerUsernameAndDueDateBeforeAndStatusNotIn(
                eq(USERNAME), any(LocalDateTime.class), anyList()))
                .thenReturn(List.of(task));

        List<Task> result = taskService.getOverdueTasks(USERNAME);

        assertEquals(1, result.size());

        verify(taskRepository).findAllByOwnerUsernameAndDueDateBeforeAndStatusNotIn(
                eq(USERNAME), any(LocalDateTime.class), anyList());
    }

    @Test
    void getTaskStatisticsShouldReturnAllCounters() {
        when(taskRepository.countByOwnerUsername(USERNAME)).thenReturn(11L);
        when(taskRepository.countByOwnerUsernameAndStatus(USERNAME, TaskStatus.NEW))
                .thenReturn(3L);
        when(taskRepository.countByOwnerUsernameAndStatus(USERNAME, TaskStatus.IN_PROGRESS))
                .thenReturn(2L);
        when(taskRepository.countByOwnerUsernameAndStatus(USERNAME, TaskStatus.COMPLETED))
                .thenReturn(5L);
        when(taskRepository.countByOwnerUsernameAndStatus(USERNAME, TaskStatus.CANCELLED))
                .thenReturn(1L);
        when(taskRepository.countByOwnerUsernameAndDueDateBeforeAndStatusNotIn(
                eq(USERNAME), any(LocalDateTime.class), anyList()))
                .thenReturn(2L);
        when(taskRepository.countByOwnerUsernameAndStatusAndUpdatedAtAfter(
                eq(USERNAME), eq(TaskStatus.COMPLETED), any(LocalDateTime.class)))
                .thenReturn(4L);

        TaskStatisticsResponse result = taskService.getTaskStatistics(USERNAME);

        assertEquals(11L, result.total());
        assertEquals(3L, result.newTasks());
        assertEquals(2L, result.inProgress());
        assertEquals(5L, result.completed());
        assertEquals(1L, result.cancelled());
        assertEquals(2L, result.overdue());
        assertEquals(4L, result.completedLast7Days());
    }
}
