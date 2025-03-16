package team.cclucky.parallel.master;

import org.apache.dubbo.config.annotation.DubboService;
import team.cclucky.parallel.core.Worker;
import team.cclucky.parallel.core.model.WorkerStatus;
import team.cclucky.parallel.core.registry.IWorkerRegistry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author cclucky
 */
@DubboService
public class WorkerRegistry implements IWorkerRegistry {
    private final static Map<String, Worker> workers = new ConcurrentHashMap<>();

    @Override
    public List<Worker> getAvailableWorkers() {
        return workers.values().stream()
            .filter(Worker::isAvailable)
            .collect(Collectors.toList());
    }

    @Override
    public void addWorker(Worker worker) {
        if (!workers.containsKey(worker.getWorkerId()) && worker.getStatus() == WorkerStatus.IDLE) {
            workers.put(worker.getWorkerId(), worker);
        }
    }

    @Override
    public void updateWorkerStatus(String workerId, WorkerStatus status) {
        Worker worker = workers.get(workerId);
        if (worker != null) {
            worker.setStatus(status);
        }
    }

    @Override
    public void markWorkerAsOffline(String workerId) {
        updateWorkerStatus(workerId, WorkerStatus.STOPPED);
    }

    @Override
    public Worker removeWorker(String workerId) {
        return workers.remove(workerId);
    }

    @Override
    public Worker getWorker(String workerId) {
        return workers.get(workerId);
    }

    @Override
    public boolean hasWorker(String workerId) {
        return workers.containsKey(workerId);
    }
} 