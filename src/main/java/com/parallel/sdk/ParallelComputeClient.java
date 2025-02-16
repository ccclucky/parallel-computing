package com.parallel.sdk;

import java.util.concurrent.CompletableFuture;

import com.parallel.core.Task;
import com.parallel.core.TaskResult;
import com.parallel.core.TaskStatus;
import com.parallel.master.TaskSubmitter;

public class ParallelComputeClient {
    private final TaskSubmitter taskSubmitter;
    private final String endpoint;
    private final ClientConfig config;
    private volatile boolean isRunning = true;
    
    public ParallelComputeClient(String endpoint, TaskSubmitter taskSubmitter, ClientConfig config) {
        this.endpoint = endpoint;
        this.taskSubmitter = taskSubmitter;
        this.config = config;
    }
    
    public <T> CompletableFuture<TaskResult<T>> submit(Task<T> task, ClientConfig config) {
        if (!isRunning) {
            throw new IllegalStateException("Client is closed");
        }
        
        // 设置任务配置
        task.setConfig(config.toTaskConfig());
        
        // 提交任务到集群
        return taskSubmitter.submit(task, config)
            .whenComplete((result, error) -> {
                if (error != null) {
                    // 处理错误
                    handleSubmitError(task, error);
                }
            });
    }
    
    public <T> TaskResult<T> submitTaskSync(Task<T> task) {
        return submit(task, config).join();
    }
    
    public TaskStatus getTaskStatus(String taskId) {
        if (!isRunning) {
            throw new IllegalStateException("Client is closed");
        }
        return taskSubmitter.getStatus(endpoint, taskId);
    }
    
    private <T> void handleSubmitError(Task<T> task, Throwable error) {
        // 实现错误处理逻辑
        task.setStatus(TaskStatus.FAILED);
    }
    
    public void close() {
        isRunning = false;
    }
} 