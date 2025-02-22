package team.cclucky.parallel.core.registry;


import team.cclucky.parallel.core.Worker;
import team.cclucky.parallel.core.model.WorkerMetadata;
import team.cclucky.parallel.core.model.WorkerStatus;

public interface IRegistrationService {

    void registerWorker(Worker worker);

    void updateHeartbeat(String workerId, WorkerStatus status);

    void deregisterWorker(String workerId);

    void shutdown();
}
