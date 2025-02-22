package team.cclucky.parallel.core.task;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TaskConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int maxRetries = 3;
    private long timeout = 30000;
    private Map<String, Object> properties = new HashMap<>();
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    public long getTimeout() {
        return timeout;
    }
    
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
    
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }
    
    public Object getProperty(String key) {
        return properties.get(key);
    }
} 