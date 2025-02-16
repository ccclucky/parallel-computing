package com.parallel.worker;

import com.parallel.core.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TaskExceptionHandler {
    private static final Logger logger = Logger.getLogger(TaskExceptionHandler.class.getName());
    private static final int DEFAULT_RETRY_DELAY_MS = 1000;
    private static final Map<String, AtomicInteger> retryCounters = new ConcurrentHashMap<>();
    
    @SuppressWarnings("unchecked")
    public static void handleTaskException(TaskSplit<?> split, Task<?> task, Exception e) {
        String taskId = task.getTaskId();
        String errorMsg = String.format("Task %s split %s failed: %s", 
            taskId, split.getSplitId(), e.getMessage());
        logger.log(Level.SEVERE, errorMsg, e);
        
        // 更新任务状态
        task.setStatus(TaskStatus.FAILED);
        
        // 重试逻辑
        if (shouldRetry(task)) {
            retryTask((TaskSplit<Object>)split, (Task<Object>)task);
        } else {
            handleFinalFailure(task, e);
        }
    }
    
    private static boolean shouldRetry(Task<?> task) {
        TaskConfig config = task.getConfig();
        if (config == null || config.getMaxRetries() <= 0) {
            return false;
        }
        
        AtomicInteger counter = retryCounters.computeIfAbsent(
            task.getTaskId(), k -> new AtomicInteger());
        return counter.get() < config.getMaxRetries();
    }
    
    private static <T> void retryTask(TaskSplit<T> split, Task<T> task) {
        String taskId = task.getTaskId();
        AtomicInteger counter = retryCounters.get(taskId);
        int retryCount = counter.incrementAndGet();
        
        logger.info(String.format("Retrying task %s, attempt %d/%d", 
            taskId, retryCount, task.getConfig().getMaxRetries()));
        
        try {
            Thread.sleep(calculateRetryDelay(retryCount));
            task.setStatus(TaskStatus.PENDING);
            task.execute(split);
            task.setStatus(TaskStatus.COMPLETED);
            retryCounters.remove(taskId);
        } catch (Exception e) {
            if (!shouldRetry(task)) {
                handleFinalFailure(task, e);
            }
        }
    }
    
    private static long calculateRetryDelay(int retryCount) {
        // 指数退避策略
        return DEFAULT_RETRY_DELAY_MS * (long)Math.pow(2, retryCount - 1);
    }
    
    private static void handleFinalFailure(Task<?> task, Exception e) {
        String taskId = task.getTaskId();
        logger.severe(String.format("Task %s failed permanently after %d retries: %s",
            taskId, retryCounters.get(taskId).get(), e.getMessage()));
        retryCounters.remove(taskId);
        
        // 可以添加告警或其他失败处理逻辑
    }
    
    public static void clearRetryCounter(String taskId) {
        retryCounters.remove(taskId);
    }
} 