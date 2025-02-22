package team.cclucky.parallel.master;

import team.cclucky.parallel.core.task.*;

import java.util.concurrent.*;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

public class TaskStateManager {
    private final Map<String, TaskContext> taskContexts = new ConcurrentHashMap<>();
    private final Map<String, TaskStatus> taskStatuses = new ConcurrentHashMap<>();
    private final Map<String, Set<TaskStatusListener>> statusListeners = new ConcurrentHashMap<>();
    private final Map<String, TaskMetrics> taskMetrics = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public void registerTask(Task<?> task) {
        String taskId = task.getTaskId();
        TaskContext context = new TaskContext(taskId);
        taskContexts.put(taskId, context);
        taskStatuses.put(taskId, TaskStatus.CREATED);
        statusListeners.put(taskId, new CopyOnWriteArraySet<>());
        taskMetrics.put(taskId, new TaskMetrics(taskId));
        
        // 启动任务超时检查
        scheduleTimeoutCheck(task);
    }
    
    private void scheduleTimeoutCheck(Task<?> task) {
        TaskConfig config = task.getConfig();
        if (config != null && config.getTimeout() > 0) {
            scheduler.schedule(() -> {
                if (taskStatuses.get(task.getTaskId()) == TaskStatus.RUNNING) {
                    updateTaskStatus(task.getTaskId(), TaskStatus.FAILED);
                }
            }, config.getTimeout(), TimeUnit.MILLISECONDS);
        }
    }
    
    public void updateTaskStatus(String taskId, TaskStatus newStatus) {
        TaskStatus oldStatus = taskStatuses.get(taskId);
        if (oldStatus != newStatus) {
            taskStatuses.put(taskId, newStatus);
            updateMetrics(taskId, newStatus);
            notifyStatusChange(taskId, oldStatus, newStatus);
        }
    }
    
    private void updateMetrics(String taskId, TaskStatus newStatus) {
        TaskMetrics metrics = taskMetrics.get(taskId);
        if (metrics != null) {
            metrics.updateStatus(newStatus);
        }
    }
    
    public void addStatusListener(String taskId, TaskStatusListener listener) {
        statusListeners.computeIfAbsent(taskId, k -> new CopyOnWriteArraySet<>())
            .add(listener);
    }
    
    public void removeStatusListener(String taskId, TaskStatusListener listener) {
        Set<TaskStatusListener> listeners = statusListeners.get(taskId);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }
    
    private void notifyStatusChange(String taskId, TaskStatus oldStatus, TaskStatus newStatus) {
        Set<TaskStatusListener> listeners = statusListeners.get(taskId);
        if (listeners != null) {
            listeners.forEach(listener -> 
                listener.onStatusChange(taskId, oldStatus, newStatus));
        }
    }
    
    public TaskContext getTaskContext(String taskId) {
        return taskContexts.get(taskId);
    }
    
    public TaskStatus getTaskStatus(String taskId) {
        return taskStatuses.getOrDefault(taskId, TaskStatus.FAILED);
    }
    
    public TaskMetrics getTaskMetrics(String taskId) {
        return taskMetrics.get(taskId);
    }
    
    public List<String> getRunningTasks() {
        List<String> runningTasks = new ArrayList<>();
        taskStatuses.forEach((taskId, status) -> {
            if (status == TaskStatus.RUNNING) {
                runningTasks.add(taskId);
            }
        });
        return runningTasks;
    }
    
    public void removeTask(String taskId) {
        taskContexts.remove(taskId);
        taskStatuses.remove(taskId);
        statusListeners.remove(taskId);
        taskMetrics.remove(taskId);
    }
    
    public void shutdown() {
        scheduler.shutdown();
    }
} 