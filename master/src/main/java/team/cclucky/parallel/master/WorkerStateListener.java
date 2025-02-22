package team.cclucky.parallel.master;

import team.cclucky.parallel.core.Worker;

public interface WorkerStateListener {
    void onWorkerAdded(Worker worker);
    void onWorkerUpdated(Worker worker);
    void onWorkerRemoved(Worker worker);
    void onWorkerOffline(Worker worker);
} 