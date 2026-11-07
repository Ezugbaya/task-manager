package com.javarush.taskmanager.repository;

import com.javarush.taskmanager.entity.Task;
import com.javarush.taskmanager.entity.TaskStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByOwnerUsername(String username, Sort sort);

    Optional<Task> findByIdAndOwnerUsername(Long id, String username);

    List<Task> findAllByOwnerUsernameAndStatus(
            String username,
            TaskStatus status
    );

    List<Task> findAllByOwnerUsernameAndDueDateBeforeAndStatusNotIn(
            String username,
            LocalDateTime date,
            List<TaskStatus> excludedStatuses
    );

    long countByOwnerUsername(String username);

    long countByOwnerUsernameAndStatus(
            String username,
            TaskStatus status
    );

    long countByOwnerUsernameAndDueDateBeforeAndStatusNotIn(
            String username,
            LocalDateTime date,
            List<TaskStatus> excludedStatuses
    );

    long countByOwnerUsernameAndStatusAndUpdatedAtAfter(
            String username,
            TaskStatus status,
            LocalDateTime date
    );
}
