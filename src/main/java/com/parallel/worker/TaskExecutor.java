package com.parallel.worker;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.parallel.core.Task;
import com.parallel.core.TaskContext;
import com.parallel.core.TaskResult;
import com.parallel.core.TaskSplit;
import com.parallel.core.TaskStatus;

public class TaskExecutor {
    private final ExecutorService executorService;
    private final TaskResultCache resultCache;
    private final MetricsCollector metricsCollector;
    
    public TaskExecutor(int threadPoolSize) {
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        this.resultCache = new TaskResultCache();
        this.metricsCollector = new MetricsCollector();
    }
    
    public <T> CompletableFuture<TaskResult<T>> executeTask(
            TaskSplit<T> split, 
            Task<T> task,
            TaskContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. 执行前准备
                metricsCollector.beginTask(split.getSplitId());
                task.setStatus(TaskStatus.RUNNING);
                
                // 2. 执行任务
                TaskResult<T> result = task.execute(split);
                
                // 3. 缓存结果
                resultCache.put(split.getSplitId(), result);
                
                // 4. 收集指标
                metricsCollector.endTask(split.getSplitId());
                result.setMetrics(metricsCollector.getMetrics(split.getSplitId()));
                
                return result;
            } catch (Exception e) {
                TaskExceptionHandler.handleTaskException(split, task, e);
                throw e;
            }
        }, executorService);
    }
    
    public void shutdown() {
        executorService.shutdown();
    }
} 