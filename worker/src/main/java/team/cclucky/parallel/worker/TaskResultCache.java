package team.cclucky.parallel.worker;

import team.cclucky.parallel.core.task.TaskResult;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class TaskResultCache {
    private final Map<String, TaskResult<?>> cache = new ConcurrentHashMap<>();
    
    public void put(String splitId, TaskResult<?> result) {
        cache.put(splitId, result);
    }
    
    public TaskResult<?> get(String splitId) {
        return cache.get(splitId);
    }
    
    public void remove(String splitId) {
        cache.remove(splitId);
    }
} 