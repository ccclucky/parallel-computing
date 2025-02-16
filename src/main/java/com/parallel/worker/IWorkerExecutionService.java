package com.parallel.worker.service;

import com.parallel.core.TaskContext;
import com.parallel.core.TaskResult;
import com.parallel.core.TaskSplit;

public interface IWorkerExecutionService {
    <T> TaskResult<T> execute(TaskSplit<T> split, TaskContext context);
} 