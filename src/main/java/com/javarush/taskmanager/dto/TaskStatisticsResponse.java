
package com.javarush.taskmanager.dto;

public record TaskStatisticsResponse(
        long total,
        long newTasks,
        long inProgress,
        long completed,
        long cancelled,
        long overdue,
        long completedLast7Days
) {
}
