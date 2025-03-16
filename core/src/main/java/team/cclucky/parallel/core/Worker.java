package team.cclucky.parallel.core;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import team.cclucky.parallel.core.model.WorkerMetadata;
import team.cclucky.parallel.core.model.WorkerStatus;
import team.cclucky.parallel.core.registry.IWorkerRegistry;

/**
 * @author cclucky
 */
public class Worker implements Serializable {
    private static final Logger logger = Logger.getLogger(Worker.class.getName());

    protected final String workerId;
    protected final String masterEndpoint;
    protected final WorkerMetadata metadata;
    protected volatile WorkerStatus status;

    // 标记不需要序列化的字段
    protected transient IWorkerRegistry workerRegistryService;

    public Worker(String masterEndpoint) {
        this.workerId = generateWorkerId();
        this.masterEndpoint = masterEndpoint;
        this.metadata = createWorkerMetadata();
        this.status = WorkerStatus.IDLE;
    }

    public String getWorkerId() { return workerId; }
    public String getMasterEndpoint() { return masterEndpoint; }
    public WorkerMetadata getMetadata() { return metadata; }
    public WorkerStatus getStatus() { return status; }

    public void setStatus(WorkerStatus newStatus) {
        WorkerStatus oldStatus = this.status;
        this.status = newStatus;
        logger.info(String.format("Worker %s status changed: %s -> %s",
                workerId, oldStatus, newStatus));
    }

    private String generateWorkerId() {
        return "worker-" + UUID.randomUUID().toString();
    }

    private WorkerMetadata createWorkerMetadata() {
        return new WorkerMetadata(
                Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().maxMemory(),
                System.getProperty("os.name"),
                System.getProperty("java.version"),
                getHostName()
        );
    }

    private String getHostName() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }

    public boolean isAvailable() {
        return status == WorkerStatus.IDLE || status == WorkerStatus.RUNNING;
    }

}