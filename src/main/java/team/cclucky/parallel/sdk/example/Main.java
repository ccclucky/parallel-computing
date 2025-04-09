package team.cclucky.parallel.sdk.example;

import team.cclucky.parallel.worker.WorkerNode;

public class Main {
    public static void main(String[] args) {
        WorkerNode masterNode = new WorkerNode("localhost:8080");
        masterNode.start();
    }
}
