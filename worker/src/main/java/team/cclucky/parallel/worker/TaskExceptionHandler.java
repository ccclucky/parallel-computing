package team.cclucky.parallel.worker;

import team.cclucky.parallel.core.task.TaskSplit;
import team.cclucky.parallel.core.task.TaskSplitStatus;

import java.io.Serializable;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author cclucky
 */
public class TaskExceptionHandler implements Serializable {
    private static final Logger logger = Logger.getLogger(TaskExceptionHandler.class.getName());
    private static final int DEFAULT_RETRY_DELAY_MS = 1000;
    private static final Map<String, Map<String, AtomicInteger>> retryCounters = new ConcurrentHashMap<>();
    
    @SuppressWarnings("unchecked")
    public static void handleTaskException(TaskSplit<?> split, Exception e) {
        String taskId = split.getTaskId();
        String errorMsg = String.format("Task %s split %s failed: %s", 
            taskId, split.getSplitId(), e.getMessage());
        logger.log(Level.SEVERE, errorMsg, e);
        
        // 更新任务状态
        split.setStatus(TaskSplitStatus.FAILED);
        
        // 重试逻辑
        if (shouldRetry(split)) {
            retryTask((TaskSplit<Object>)split);
        } else {
            handleFinalFailure(split, e);
        }
    }
    
    private static boolean shouldRetry(TaskSplit<?> taskSplit) {
        int maxRetries = taskSplit.getMaxRetries();
        if (maxRetries <= 0) {
            return false;
        }

        Map<String, AtomicInteger> taskCounters = retryCounters.computeIfAbsent(
                taskSplit.getTaskId(), k -> new ConcurrentHashMap<>());
        AtomicInteger counter = taskCounters.computeIfAbsent(taskSplit.getSplitId(), k -> new AtomicInteger());
        return counter.get() < maxRetries;
    }
    
    private static <T> void retryTask(TaskSplit<T> split) {
        String taskId = split.getTaskId();
        int retryCount = retryCounters.computeIfAbsent(taskId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(split.getSplitId(), k -> new AtomicInteger()).incrementAndGet();
        logger.info(String.format("Retrying task %s split %s, attempt %d/%d",
            taskId, split.getSplitId(), retryCount, split.getMaxRetries()));

        try {
            Thread.sleep(calculateRetryDelay(retryCount));
            split.setStatus(TaskSplitStatus.PENDING);
            split.getProcessor().execute(split);
            split.setStatus(TaskSplitStatus.COMPLETED);
            retryCounters.get(taskId).remove(split.getSplitId());
        } catch (Exception e) {
            if (!shouldRetry(split)) {
                handleFinalFailure(split, e);
            }
        }
    }
    
    private static long calculateRetryDelay(int retryCount) {
        // 指数退避策略
        return DEFAULT_RETRY_DELAY_MS * (long)Math.pow(2, retryCount - 1);
    }
    
    private static void handleFinalFailure(TaskSplit<?> taskSplit, Exception e) {
        String taskId = taskSplit.getTaskId();
        logger.severe(String.format("Task %s failed permanently after %d retries: %s",
            taskId, retryCounters.get(taskId).get(taskSplit.getSplitId()).get(), e.getMessage()));
        retryCounters.remove(taskId);
        
        // 可以添加告警或其他失败处理逻辑
    }
    
    public static void clearRetryCounter(String taskId) {
        retryCounters.remove(taskId);
    }
} 