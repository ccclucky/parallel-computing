package team.cclucky.parallel.core.registry;

import team.cclucky.parallel.core.Worker;
import team.cclucky.parallel.core.model.WorkerStatus;

import java.util.List;
import java.util.stream.Collectors;

public interface IWorkerRegistry {

    List<Worker> getAvailableWorkers();

    void addWorker(Worker worker);

    void updateWorkerStatus(String workerId, WorkerStatus status);

    void markWorkerAsOffline(String workerId);

    Worker removeWorker(String workerId);

    Worker getWorker(String workerId);

    boolean hasWorker(String workerId);
}
