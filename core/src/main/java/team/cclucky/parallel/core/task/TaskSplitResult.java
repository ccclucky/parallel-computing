package team.cclucky.parallel.core.task;

import java.io.Serializable;
import java.util.Map;

/**
 * @author cclucky
 */
public class TaskSplitResult<T> implements Serializable {
    private String taskId;
    private String splitId;
    private T result;
    private TaskSplitStatus status;
    private long startTime;
    private long endTime;
    private Map<String, Object> metrics;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getSplitId() {
        return splitId;
    }

    public void setSplitId(String splitId) {
        this.splitId = splitId;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public TaskSplitStatus getStatus() {
        return status;
    }

    public void setStatus(TaskSplitStatus status) {
        this.status = status;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public Map<String, Object> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, Object> metrics) {
        this.metrics = metrics;
    }

}