package com.parallel.master;

import com.parallel.worker.WorkerNode;
import com.parallel.common.model.WorkerStatus;
import com.parallel.common.model.WorkerMetadata;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class WorkerRegistry {
    private final Map<String, WorkerNode> workers = new ConcurrentHashMap<>();
    
    public List<WorkerNode> getAvailableWorkers() {
        return workers.values().stream()
            .filter(WorkerNode::isAvailable)
            .collect(Collectors.toList());
    }
    
    public void addWorker(String workerId, String endpoint, WorkerMetadata metadata) {
        WorkerNode worker = new WorkerNode(endpoint);
        worker.setStatus(WorkerStatus.IDLE);
        workers.put(workerId, worker);
    }
    
    public void updateWorkerStatus(String workerId, WorkerStatus status) {
        WorkerNode worker = workers.get(workerId);
        if (worker != null) {
            worker.setStatus(status);
        }
    }
    
    public void markWorkerAsOffline(String workerId) {
        updateWorkerStatus(workerId, WorkerStatus.STOPPED);
    }
    
    public WorkerNode removeWorker(String workerId) {
        return workers.remove(workerId);
    }
    
    public WorkerNode getWorker(String workerId) {
        return workers.get(workerId);
    }
    
    public boolean hasWorker(String workerId) {
        return workers.containsKey(workerId);
    }
} 