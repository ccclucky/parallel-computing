package com.parallel.registry;

import com.parallel.common.model.WorkerMetadata;
import com.parallel.common.model.WorkerStatus;

public interface IRegistrationService {

    void registerWorker(String workerId, String endpoint, WorkerMetadata metadata);

    void updateHeartbeat(String workerId, WorkerStatus status);

    void deregisterWorker(String workerId);
}
