package team.cclucky.sdk.example;

import team.cclucky.parallel.worker.WorkerNode;

/**
 * @author cclucky
 */
public class WorkerNodeExample {
    public static void main(String[] args) {
        WorkerNode workerNode = new WorkerNode("localhost:8080");
        workerNode.start();
    }
}
