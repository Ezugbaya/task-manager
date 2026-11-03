package com.javarush.taskmanager.repository;

import com.javarush.taskmanager.entity.Task;
import com.javarush.taskmanager.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByDueDateBefore(java.time.LocalDateTime date);
}