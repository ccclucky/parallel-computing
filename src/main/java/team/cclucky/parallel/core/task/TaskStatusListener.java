package team.cclucky.parallel.core.task;


public interface TaskStatusListener {
    void onStatusChange(String taskId, TaskStatus oldStatus, TaskStatus newStatus);
} 