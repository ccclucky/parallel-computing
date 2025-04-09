package team.cclucky.parallel.worker.rpc.consumer;//package team.cclucky.parallel.worker.rpc.consumer;
//
//import team.cclucky.parallel.core.Worker;
//import team.cclucky.parallel.core.model.WorkerStatus;
//
//import java.io.Serializable;
//import java.util.Map;
//import java.util.concurrent.*;
//import java.util.logging.Logger;
//
///**
// * @author cclucky
// */
//public class RegistrationService implements Serializable {
//    private static final Logger logger = Logger.getLogger(RegistrationService.class.getName());
//    private static final long HEARTBEAT_TIMEOUT_MS = 30000;
//
//    private final Map<String, Long> lastHeartbeats = new ConcurrentHashMap<>();
//    private transient final ScheduledExecutorService scheduler;
//
//    public RegistrationService() {
//        this.scheduler = Executors.newScheduledThreadPool(1);
//        startHeartbeatChecker();
//    }
//
//    public void registerWorker(Worker worker) {
//        lastHeartbeats.put(worker.getWorkerId(), System.currentTimeMillis());
//    }
//
//    public void updateHeartbeat(String workerId, WorkerStatus status) {
//        lastHeartbeats.put(workerId, System.currentTimeMillis());
//        registry.updateWorkerStatus(workerId, status);
//    }
//
//    private void startHeartbeatChecker() {
//        scheduler.scheduleAtFixedRate(() -> {
//            long now = System.currentTimeMillis();
//            lastHeartbeats.forEach((workerId, lastHeartbeat) -> {
//                if (now - lastHeartbeat > HEARTBEAT_TIMEOUT_MS) {
//                    registry.markWorkerAsOffline(workerId);
//                }
//            });
//        }, HEARTBEAT_TIMEOUT_MS, HEARTBEAT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
//    }
//
//    public void shutdown() {
//        scheduler.shutdown();
//    }
//}