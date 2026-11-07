package com.javarush.taskmanager.service;

import com.javarush.taskmanager.dto.TaskStatisticsResponse;
import com.javarush.taskmanager.entity.Task;
import com.javarush.taskmanager.entity.TaskPriority;
import com.javarush.taskmanager.entity.TaskStatus;
import com.javarush.taskmanager.entity.User;
import com.javarush.taskmanager.exception.NotFoundException;
import com.javarush.taskmanager.repository.TaskRepository;
import com.javarush.taskmanager.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(
            TaskRepository taskRepository,
            UserRepository userRepository
    ) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public List<Task> getAllTasks(
            String username,
            String sortBy,
            String direction
    ) {
        List<String> allowedFields =
                List.of("createdAt", "dueDate", "priority", "updatedAt", "title");

        if (!allowedFields.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Недопустимое поле сортировки: " + sortBy
            );
        }

        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Направление сортировки должно быть ASC или DESC"
            );
        }

        return taskRepository.findAllByOwnerUsername(
                username,
                Sort.by(sortDirection, sortBy)
        );
    }

    public Task getTaskById(Long id, String username) {
        return taskRepository.findByIdAndOwnerUsername(id, username)
                .orElseThrow(() ->
                        new NotFoundException("Задача не найдена: " + id)
                );
    }

    public Task createTask(Task task, String username) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new NotFoundException("Пользователь не найден")
                );

        task.setOwner(owner);

        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.NEW);
        }
        if (task.getPriority() == null) {
            task.setPriority(TaskPriority.MEDIUM);
        }

        return taskRepository.save(task);
    }

    public Task updateTask(Long id, Task updatedTask, String username) {
        Task task = getTaskById(id, username);

        task.setTitle(updatedTask.getTitle());
        task.setDescription(updatedTask.getDescription());
        task.setDueDate(updatedTask.getDueDate());

        if (updatedTask.getStatus() != null) {
            task.setStatus(updatedTask.getStatus());
        }

        if (updatedTask.getPriority() != null) {
            task.setPriority(updatedTask.getPriority());
        }

        return taskRepository.save(task);
    }

    public void deleteTask(Long id, String username) {
        Task task = getTaskById(id, username);
        taskRepository.delete(task);
    }

    public List<Task> getTasksByStatus(
            TaskStatus status,
            String username
    ) {
        return taskRepository.findAllByOwnerUsernameAndStatus(
                username,
                status
        );
    }

    public List<Task> getOverdueTasks(String username) {
        return taskRepository
                .findAllByOwnerUsernameAndDueDateBeforeAndStatusNotIn(
                        username,
                        LocalDateTime.now(),
                        List.of(TaskStatus.COMPLETED, TaskStatus.CANCELLED)
                );
    }

    public TaskStatisticsResponse getTaskStatistics(String username) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusDays(7);

        return new TaskStatisticsResponse(
                taskRepository.countByOwnerUsername(username),
                taskRepository.countByOwnerUsernameAndStatus(
                        username, TaskStatus.NEW
                ),
                taskRepository.countByOwnerUsernameAndStatus(
                        username, TaskStatus.IN_PROGRESS
                ),
                taskRepository.countByOwnerUsernameAndStatus(
                        username, TaskStatus.COMPLETED
                ),
                taskRepository.countByOwnerUsernameAndStatus(
                        username, TaskStatus.CANCELLED
                ),
                taskRepository
                        .countByOwnerUsernameAndDueDateBeforeAndStatusNotIn(
                                username,
                                now,
                                List.of(
                                        TaskStatus.COMPLETED,
                                        TaskStatus.CANCELLED
                                )
                        ),
                taskRepository.countByOwnerUsernameAndStatusAndUpdatedAtAfter(
                        username,
                        TaskStatus.COMPLETED,
                        weekAgo
                )
        );
    }
}
