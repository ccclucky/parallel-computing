package com.parallel.registry;

import com.parallel.common.model.WorkerMetadata;
import com.parallel.common.model.WorkerStatus;
import com.parallel.master.WorkerRegistry;

import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class RegistrationService implements IRegistrationService {
    private static final Logger logger = Logger.getLogger(RegistrationService.class.getName());
    private static final long HEARTBEAT_TIMEOUT_MS = 30000;
    
    private final WorkerRegistry registry;
    private final Map<String, Long> lastHeartbeats = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;
    
    public RegistrationService(WorkerRegistry registry) {
        this.registry = registry;
        this.scheduler = Executors.newScheduledThreadPool(1);
        startHeartbeatChecker();
    }
    
    public void registerWorker(String workerId, String endpoint, WorkerMetadata metadata) {
        boolean isNewWorker = !registry.hasWorker(workerId);
        registry.addWorker(workerId, endpoint, metadata);
        lastHeartbeats.put(workerId, System.currentTimeMillis());
        
        logger.info(isNewWorker ? 
            "New worker registered: " + workerId : 
            "Worker re-registered: " + workerId);
    }
    
    public void updateHeartbeat(String workerId, WorkerStatus status) {
        lastHeartbeats.put(workerId, System.currentTimeMillis());
        registry.updateWorkerStatus(workerId, status);
    }
    
    private void startHeartbeatChecker() {
        scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            lastHeartbeats.forEach((workerId, lastHeartbeat) -> {
                if (now - lastHeartbeat > HEARTBEAT_TIMEOUT_MS) {
                    registry.markWorkerAsOffline(workerId);
                }
            });
        }, HEARTBEAT_TIMEOUT_MS, HEARTBEAT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }
    
    public void shutdown() {
        scheduler.shutdown();
    }
    
    public void deregisterWorker(String workerId) {
        registry.removeWorker(workerId);
        lastHeartbeats.remove(workerId);
        logger.info("Worker deregistered: " + workerId);
    }
} 