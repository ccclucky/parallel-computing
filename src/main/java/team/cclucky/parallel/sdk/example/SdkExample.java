package team.cclucky.parallel.sdk.example;

import team.cclucky.parallel.core.config.ClientConfig;
import team.cclucky.parallel.core.task.TaskResult;
import team.cclucky.parallel.master.MasterNode;
import team.cclucky.parallel.worker.WorkerNode;
import team.cclucky.sdk.ParallelComputeClient;

import java.util.Arrays;
import java.util.List;

public class SdkExample {
    public static void main(String[] args) {
        // 1. 创建客户端配置
        ClientConfig config = new ClientConfig.Builder()
            .withMaxRetries(3)
            .withTimeout(30000)
            .withCredentials("your-access-key", "your-secret-key")
            .build();
            
        // 1. 获取Master节点实例
        MasterNode masterNode = new MasterNode(8080);
        masterNode.start();

        WorkerNode workerNode = new WorkerNode("localhost:8080");
        workerNode.start();

        // 2. 初始化客户端
        ParallelComputeClient client = new ParallelComputeClient(
            "parallel-compute.example.com:8080",
            masterNode.getTaskSubmitter(),
            config
        );
        
        // 3. 准备输入数据
        List<String> inputData = Arrays.asList(
            "item1", "item2", "item3", "item4", "item5"
        );
        
        // 4. 创建并提交任务
        DataProcessTask task = new DataProcessTask();
        task.setInput(inputData);
        
        // 5. 异步执行
//        client.submit(task, config)
//            .thenAccept(result -> {
//                System.out.println("Task completed: " + result.getResult());
//            })
//            .exceptionally(e -> {
//                System.err.println("Task failed: " + e.getMessage());
//                return null;
//            });
            
        // 或者同步执行
        TaskResult<List<String>> result = client.submitTaskSync(task);
        System.out.println("Result: " + result.getResult());
    }
} 