package team.cclucky.parallel.core.task;

import java.util.HashMap;
import java.util.Map;

public class TaskContext {
    private final String taskId;
    private final Map<String, Object> contextData = new HashMap<>();
    
    public TaskContext(String taskId) {
        this.taskId = taskId;
    }
    
    public String getTaskId() {
        return taskId;
    }
    
    public void setAttribute(String key, Object value) {
        contextData.put(key, value);
    }
    
    public Object getAttribute(String key) {
        return contextData.get(key);
    }
} 