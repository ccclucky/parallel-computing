package team.cclucky.parallel.master;

import team.cclucky.parallel.core.task.TaskStatus;
import java.util.concurrent.atomic.AtomicLong;

public class TaskMetrics {
    private final String taskId;
    private final long createTime;
    private final AtomicLong startTime = new AtomicLong();
    private final AtomicLong endTime = new AtomicLong();
    private final AtomicLong retryCount = new AtomicLong();
    private volatile TaskStatus lastStatus;
    
    public TaskMetrics(String taskId) {
        this.taskId = taskId;
        this.createTime = System.currentTimeMillis();
        this.lastStatus = TaskStatus.CREATED;
    }
    
    public void updateStatus(TaskStatus newStatus) {
        if (newStatus == TaskStatus.RUNNING && startTime.get() == 0) {
            startTime.set(System.currentTimeMillis());
        } else if (newStatus.isTerminal()) {
            endTime.set(System.currentTimeMillis());
        }
        lastStatus = newStatus;
    }
    
    public void incrementRetry() {
        retryCount.incrementAndGet();
    }
    
    public long getDuration() {
        long end = endTime.get();
        if (end == 0) {
            end = System.currentTimeMillis();
        }
        return end - startTime.get();
    }
    
    // Getters
    public String getTaskId() { return taskId; }
    public long getCreateTime() { return createTime; }
    public long getStartTime() { return startTime.get(); }
    public long getEndTime() { return endTime.get(); }
    public long getRetryCount() { return retryCount.get(); }
    public TaskStatus getLastStatus() { return lastStatus; }
} 