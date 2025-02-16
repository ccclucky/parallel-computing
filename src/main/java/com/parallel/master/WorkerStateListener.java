package com.parallel.master;

import com.parallel.worker.WorkerNode;

public interface WorkerStateListener {
    void onWorkerAdded(WorkerNode worker);
    void onWorkerUpdated(WorkerNode worker);
    void onWorkerRemoved(WorkerNode worker);
    void onWorkerOffline(WorkerNode worker);
} 