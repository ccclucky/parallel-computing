package team.cclucky.parallel.core.registry;

import team.cclucky.parallel.core.Worker;
import team.cclucky.parallel.core.model.WorkerStatus;

import java.io.Serializable;
import java.util.List;

/**
 * @author cclucky
 */
public interface IWorkerRegistry {

    List<Worker> getAvailableWorkers();

    void addWorker(Worker worker);

    void updateWorkerStatus(String workerId, WorkerStatus status);

    void markWorkerAsOffline(String workerId);

    Worker removeWorker(String workerId);

    Worker getWorker(String workerId);

    boolean hasWorker(String workerId);
}
