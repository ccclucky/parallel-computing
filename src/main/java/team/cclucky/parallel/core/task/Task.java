package team.cclucky.parallel.core.task;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;


import java.util.logging.Level;

/**
 * @author cclucky
 */
public abstract class Task<T> extends BaseTask<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(Task.class.getName());
    
    private String taskId;
    private String taskName;
    private TaskStatus status;
    private TaskPriority priority;
    
    // 任务配置
    private TaskConfig config;
    
    private T input;
    
    private final List<TaskStatusListener> statusListeners = new CopyOnWriteArrayList<>();
    
    protected Task(TaskProcessor<T> processor) {
        super(processor);
        this.status = TaskStatus.CREATED;
        this.priority = TaskPriority.NORMAL;
    }
    
    // 任务状态回调
    public void onStatusChange(TaskStatus oldStatus, TaskStatus newStatus) {
        logger.log(Level.INFO, "Task {0} status changed from {1} to {2}", 
            new Object[]{taskId, oldStatus, newStatus});
            
        // 状态转换验证
        if (!isValidStatusTransition(oldStatus, newStatus)) {
            logger.warning("Invalid status transition from " + oldStatus + " to " + newStatus);
            return;
        }
        
        // 特定状态处理
        switch (newStatus) {
            case CREATED:
                handleCreatedStatus();
                break;
            case PENDING:
                handlePendingStatus();
                break;
            case RUNNING:
                handleRunningStatus();
                break;
            case COMPLETED:
                handleCompletedStatus();
                break;
            case FAILED:
                handleFailedStatus();
                break;
            case CANCELLED:
                handleCancelledStatus();
                break;
        }
        
        // 通知所有监听器
        notifyStatusListeners(oldStatus, newStatus);
    }
    
    private boolean isValidStatusTransition(TaskStatus oldStatus, TaskStatus newStatus) {
        if (oldStatus == newStatus) {
            return true;
        }
        
        switch (oldStatus) {
            case CREATED:
                return newStatus == TaskStatus.PENDING;
            case PENDING:
                return newStatus == TaskStatus.RUNNING || newStatus == TaskStatus.CANCELLED;
            case RUNNING:
                return newStatus == TaskStatus.COMPLETED || newStatus == TaskStatus.FAILED 
                    || newStatus == TaskStatus.CANCELLED;
            case FAILED:
                return newStatus == TaskStatus.PENDING; // 允许重试
            case COMPLETED:
            case CANCELLED:
                return false; // 终态不允许转换
            default:
                return false;
        }
    }
    
    private void handleCreatedStatus() {
        // 任务创建时的初始化
        logger.info("Task " + taskId + " created");
        if (taskName == null) {
            taskName = "Task-" + taskId;
        }
        // 初始化任务相关资源
        initializeResources();
    }
    
    private void handlePendingStatus() {
        // 任务进入等待状态
        logger.info("Task " + taskId + " is pending");
        // 验证任务配置
        validateTaskConfig();
        // 准备执行环境
        prepareExecutionEnvironment();
    }
    
    private void handleRunningStatus() {
        // 任务开始执行
        logger.info("Task " + taskId + " is running");
        // 启动性能监控
        startPerformanceMonitoring();
        // 启动超时监控
        if (config != null && config.getTimeout() > 0) {
            startTimeoutMonitor();
        }
    }
    
    private void handleCompletedStatus() {
        // 任务完成
        logger.info("Task " + taskId + " completed");
        // 停止所有监控
        stopMonitoring();
        // 保存执行结果
        saveResults();
        // 清理资源
        cleanup();
    }
    
    private void handleFailedStatus() {
        // 任务失败
        logger.log(Level.SEVERE, "Task {0} failed", taskId);
        // 记录失败原因
        recordFailureReason();
        // 停止所有监控
        stopMonitoring();
        // 清理资源
        cleanup();
    }
    
    private void handleCancelledStatus() {
        // 任务取消
        logger.info("Task " + taskId + " cancelled");
        // 停止所有监控
        stopMonitoring();
        // 清理资源
        cleanup();
    }
    
    private void startTimeoutMonitor() {
        // 启动超时监控
        new Thread(() -> {
            try {
                Thread.sleep(config.getTimeout());
                if (status == TaskStatus.RUNNING) {
                    setStatus(TaskStatus.FAILED);
                    logger.warning("Task " + taskId + " timed out after " + config.getTimeout() + "ms");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    private void initializeResources() {
        try {
            // 初始化任务所需的资源
            logger.fine("Initializing resources for task " + taskId);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize resources for task " + taskId, e);
            setStatus(TaskStatus.FAILED);
        }
    }
    
    private void validateTaskConfig() {
        if (config == null) {
            config = new TaskConfig(); // 使用默认配置
        }
        // 验证必要的配置参数
        if (config.getTimeout() <= 0) {
            logger.warning("Task " + taskId + " has no timeout set, using default");
            config.setTimeout(30000); // 默认30秒超时
        }
    }
    
    private void prepareExecutionEnvironment() {
        try {
            // 准备执行环境，如创建临时目录等
            logger.fine("Preparing execution environment for task " + taskId);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to prepare execution environment for task " + taskId, e);
            setStatus(TaskStatus.FAILED);
        }
    }
    
    private void startPerformanceMonitoring() {
        // 启动性能监控线程
        new Thread(() -> {
            while (status == TaskStatus.RUNNING) {
                try {
                    collectPerformanceMetrics();
                    Thread.sleep(1000); // 每秒收集一次
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "PerformanceMonitor-" + taskId).start();
    }
    
    private void collectPerformanceMetrics() {
        // 收集性能指标
        // 如CPU使用率、内存使用等
    }
    
    private void stopMonitoring() {
        // 停止所有监控线程
        logger.fine("Stopping monitoring for task " + taskId);
    }
    
    private void saveResults() {
        try {
            // 保存任务执行结果
            logger.fine("Saving results for task " + taskId);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to save results for task " + taskId, e);
        }
    }
    
    private void recordFailureReason() {
        // 记录失败原因，用于后续分析
        logger.severe("Recording failure reason for task " + taskId);
    }
    
    private void cleanup() {
        try {
            // 清理临时文件
            cleanupTempFiles();
            // 释放资源
            releaseResources();
            // 清理监控数据
            cleanupMetrics();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error during cleanup for task " + taskId, e);
        }
    }
    
    private void cleanupTempFiles() {
        // 清理任务执行过程中创建的临时文件
    }
    
    private void releaseResources() {
        // 释放任务占用的资源
    }
    
    private void cleanupMetrics() {
        // 清理性能监控数据
    }
    
    public void addStatusListener(TaskStatusListener listener) {
        if (listener != null) {
            statusListeners.add(listener);
        }
    }
    
    public void removeStatusListener(TaskStatusListener listener) {
        statusListeners.remove(listener);
    }
    
    private void notifyStatusListeners(TaskStatus oldStatus, TaskStatus newStatus) {
        for (TaskStatusListener listener : statusListeners) {
            try {
                listener.onStatusChange(taskId, oldStatus, newStatus);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error notifying status listener", e);
            }
        }
    }
    
    // Getter/Setter 方法
    public String getTaskId() {
        return taskId;
    }
    
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
    
    public String getTaskName() {
        return taskName;
    }
    
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
    
    public TaskStatus getStatus() {
        return status;
    }
    
    public void setStatus(TaskStatus status) {
        TaskStatus oldStatus = this.status;
        this.status = status;
        onStatusChange(oldStatus, status);
    }
    
    public TaskPriority getPriority() {
        return priority;
    }
    
    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }
    
    public TaskConfig getConfig() {
        return config;
    }
    
    public void setConfig(TaskConfig config) {
        this.config = config;
    }
    
    public T getInput() {
        return input;
    }
    
    public void setInput(T input) {
        this.input = input;
    }
} 