package com.parallel.worker.heartbeat;

import com.parallel.worker.WorkerNode;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class HeartbeatManager {
    private static final Logger logger = Logger.getLogger(HeartbeatManager.class.getName());
    private static final long HEARTBEAT_INTERVAL = 5000; // 5秒
    
    private final WorkerNode worker;
    private final String masterEndpoint;
    private final ScheduledExecutorService scheduler;
    
    public HeartbeatManager(WorkerNode worker, String masterEndpoint) {
        this.worker = worker;
        this.masterEndpoint = masterEndpoint;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }
    
    public void start() {
        scheduler.scheduleAtFixedRate(this::sendHeartbeat, 
            0, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
        logger.info("HeartbeatManager started for worker: " + worker.getWorkerId());
    }
    
    public void stop() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("HeartbeatManager stopped for worker: " + worker.getWorkerId());
    }
    
    private void sendHeartbeat() {
        try {
            // TODO: 实现向Master发送心跳的RPC调用
            // 使用masterEndpoint作为目标地址
            String heartbeatEndpoint = masterEndpoint + "/heartbeat";
            logger.fine(String.format("Sending heartbeat to %s for worker: %s", 
                heartbeatEndpoint, worker.getWorkerId()));
        } catch (Exception e) {
            logger.warning("Failed to send heartbeat to " + masterEndpoint + ": " + e.getMessage());
        }
    }
} 