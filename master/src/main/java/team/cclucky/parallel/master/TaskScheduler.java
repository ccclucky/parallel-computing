package team.cclucky.parallel.master;

import team.cclucky.parallel.core.Worker;
import team.cclucky.parallel.core.task.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TaskScheduler {
    private final WorkerRegistry workerRegistry;
    private final LoadBalancer loadBalancer;
    private final TaskStateManager taskStateManager;
    private final TaskSubmitter taskSubmitter;

    public TaskScheduler(WorkerRegistry workerRegistry,
                         LoadBalancer loadBalancer,
                         TaskStateManager taskStateManager) {
        this.workerRegistry = workerRegistry;
        this.loadBalancer = loadBalancer;
        this.taskStateManager = taskStateManager;
        this.taskSubmitter = new TaskSubmitter(this);
    }

    public <T> CompletableFuture<TaskResult<T>> scheduleTask(Task<T> task) {
        // 1. 任务预处理
        TaskContext context = createTaskContext(task);

        // 2. 任务分片
        List<TaskSplit<T>> splits = task.split(task.getInput());

        // 3. 分配Worker节点
        Map<Worker, List<TaskSplit<T>>> allocation =
                loadBalancer.allocate(splits, workerRegistry.getAvailableWorkers());

        // 4. 提交执行
        List<CompletableFuture<List<TaskSplitResult<T>>>> futures = allocation.entrySet().stream()
                .map(entry -> taskSubmitter.submitToWorker(entry.getKey(), entry.getValue(), context))
                .collect(Collectors.toList());

        // 5. 合并结果
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> mergeResults(task, futures));
    }

    private <T> TaskResult<T> mergeResults(Task<T> task,
                                           List<CompletableFuture<List<TaskSplitResult<T>>>> futures) {
        List<TaskSplitResult<T>> results = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        return task.merge(results);
    }

    private <T> TaskContext createTaskContext(Task<T> task) {
        TaskContext context = new TaskContext(task.getTaskId());
        taskStateManager.registerTask(task);
        return context;
    }

    public TaskStatus getTaskStatus(String taskId) {
        return taskStateManager.getTaskStatus(taskId);
    }
} 