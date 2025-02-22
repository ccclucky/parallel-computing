package team.cclucky.parallel.worker.rpc.consumer;

import team.cclucky.parallel.core.Worker;
import team.cclucky.parallel.core.model.WorkerStatus;
import team.cclucky.parallel.core.registry.IRegistrationService;
import team.cclucky.parallel.core.registry.IWorkerRegistry;

import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class RegistrationService implements IRegistrationService {
    private static final Logger logger = Logger.getLogger(RegistrationService.class.getName());
    private static final long HEARTBEAT_TIMEOUT_MS = 30000;
    
    private final IWorkerRegistry registry;
    private final Map<String, Long> lastHeartbeats = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;
    
    public RegistrationService(IWorkerRegistry registry) {
        this.registry = registry;
        this.scheduler = Executors.newScheduledThreadPool(1);
        startHeartbeatChecker();
    }
    
    public void registerWorker(Worker worker) {
        boolean isNewWorker = !registry.hasWorker(worker.getWorkerId());
        registry.addWorker(worker);
        lastHeartbeats.put(worker.getWorkerId(), System.currentTimeMillis());
        
        logger.info(isNewWorker ? 
            "New worker registered: " + worker.getWorkerId() :
            "Worker re-registered: " + worker.getWorkerId());
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