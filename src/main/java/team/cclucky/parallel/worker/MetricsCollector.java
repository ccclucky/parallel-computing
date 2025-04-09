package team.cclucky.parallel.worker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MetricsCollector {
    private final Map<String, Long> taskStartTimes = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> taskMetrics = new ConcurrentHashMap<>();
    
    public void beginTask(String splitId) {
        taskStartTimes.put(splitId, System.currentTimeMillis());
    }
    
    public void endTask(String splitId) {
        Long startTime = taskStartTimes.get(splitId);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            Map<String, Object> metrics = new ConcurrentHashMap<>();
            metrics.put("duration", duration);
            taskMetrics.put(splitId, metrics);
        }
    }
    
    public Map<String, Object> getMetrics(String splitId) {
        return taskMetrics.get(splitId);
    }
} 