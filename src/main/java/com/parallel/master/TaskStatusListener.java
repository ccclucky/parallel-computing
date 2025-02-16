package com.parallel.master;

import com.parallel.core.TaskStatus;

public interface TaskStatusListener {
    void onStatusChange(String taskId, TaskStatus oldStatus, TaskStatus newStatus);
} 