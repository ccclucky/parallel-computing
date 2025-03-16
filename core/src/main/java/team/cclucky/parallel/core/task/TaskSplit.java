package team.cclucky.parallel.core.task;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;

/**
 * @author cclucky
 */
public class TaskSplit<T> extends BaseTask<T> implements Serializable {
    private String taskId;
    private String splitId;
    private T data;
    private TaskSplitStatus status;
    private int partitionNum;
    private Class<?> dataType;
    private int maxRetries = 3;
    private Map<String, Object> metadata;

    public TaskSplit(TaskProcessor<T> processor) {
        super(processor);
    }


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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public TaskSplitStatus getStatus() {
        return status;
    }

    public void setStatus(TaskSplitStatus status) {
        this.status = status;
    }

    public int getPartitionNum() {
        return partitionNum;
    }

    public void setPartitionNum(int partitionNum) {
        this.partitionNum = partitionNum;
    }

    public Class<?> getDataType() {
        return dataType;
    }

    public void setDataType(Class<?> dataType) {
        this.dataType = dataType;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}