package com.parallel.master;

import com.parallel.core.*;
import com.parallel.sdk.ClientConfig;
import com.parallel.worker.WorkerNode;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class TaskSubmitter {
    private final TaskScheduler scheduler;
    private static final Map<String, WorkerNode> workerNodes = new ConcurrentHashMap<>();
    
    public TaskSubmitter(TaskScheduler scheduler) {
        this.scheduler = scheduler;
    }
    
    public <T> CompletableFuture<TaskResult<T>> submit(Task<T> task, ClientConfig config) {
        if (task == null || config == null) {
            return CompletableFuture.completedFuture(null);
        }
        
        // 生成任务ID
        if (task.getTaskId() == null) {
            task.setTaskId(generateTaskId());
        }
        
        // 应用配置
        task.setConfig(config.toTaskConfig());
        
        // 提交任务并监控状态
        return scheduler.scheduleTask(task)
            .whenComplete((result, error) -> {
                if (error != null) {
                    task.setStatus(TaskStatus.FAILED);
                } else if (result != null) {
                    task.setStatus(TaskStatus.COMPLETED);
                }
            });
    }
    
    public <T> CompletableFuture<TaskResult<T>> submitToWorker(
            WorkerNode worker, 
            List<TaskSplit<T>> splits,
            TaskContext context) {
        CompletableFuture<TaskResult<T>> future = new CompletableFuture<>();
        
        if (worker == null || splits == null || context == null) {
            future.completeExceptionally(new IllegalArgumentException("Invalid parameters"));
            return future;
        }
        
        if (!worker.isAvailable()) {
            future.completeExceptionally(
                new IllegalStateException("Worker is not available: " + worker.getWorkerId()));
            return future;
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // TODO: 实现实际的Worker提交逻辑，例如通过RPC调用

                return null;
            } catch (Exception e) {
                future.completeExceptionally(e);
                return null;
            }
        });
    }
    
    public TaskStatus getStatus(String endpoint, String taskId) {
        if (endpoint == null || taskId == null) {
            return TaskStatus.FAILED;
        }
        return scheduler.getTaskStatus(taskId);
    }
    
    private static String generateTaskId() {
        return "task-" + System.currentTimeMillis() + "-" + 
               Math.abs(System.nanoTime() % 10000);
    }
    
    public static WorkerNode getWorker(String endpoint) {
        return endpoint != null ? workerNodes.get(endpoint) : null;
    }
} 