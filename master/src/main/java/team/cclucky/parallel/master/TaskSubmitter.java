package team.cclucky.parallel.master;

import org.apache.dubbo.common.stream.StreamObserver;
import org.apache.dubbo.config.ReferenceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.cclucky.parallel.core.Worker;
import team.cclucky.parallel.core.config.ClientConfig;
import team.cclucky.parallel.core.handle.ITaskSplitHandler;
import team.cclucky.parallel.core.task.*;
import team.cclucky.parallel.core.task.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author cclucky
 */
public class TaskSubmitter {

    private static final Logger logger = LoggerFactory.getLogger(TaskSubmitter.class);

    private final TaskScheduler scheduler;
    private ITaskSplitHandler taskSplitHandler;

    public TaskSubmitter(TaskScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public <T> CompletableFuture<TaskResult<T>> submit(Task<T> task, ClientConfig config) {
        if (task == null || config == null) {
            return CompletableFuture.completedFuture(null);
        }

        // 生成任务ID
        if (task.getTaskId() == null) {
            task.setTaskId(generateTaskId());
        }

        // 应用配置
        task.setConfig(config.toTaskConfig());

        // 提交任务并监控状态
        return scheduler.scheduleTask(task)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        task.setStatus(TaskStatus.FAILED);
                    } else if (result != null) {
                        task.setStatus(TaskStatus.COMPLETED);
                    }
                });
    }

    public <T> CompletableFuture<List<TaskSplitResult<T>>> submitToWorker(
            Worker worker,
            List<TaskSplit<T>> taskSplits,
            TaskContext context) {

        if (taskSplitHandler == null) {
            initTaskSplitHandlerReferenceConfig();
        }

        if (worker == null || taskSplits == null || context == null) {
            throw new IllegalArgumentException("Invalid parameters");
        }

        if (!worker.isAvailable()) {
            throw new IllegalStateException("Worker is not available: " + worker.getWorkerId());
        }

        // 提交所有任务
        return CompletableFuture.supplyAsync(() -> {
            ArrayList<TaskSplitResult<T>> list = new ArrayList<>();
            for (TaskSplit<T> taskSplit : taskSplits) {
                list.add(taskSplitHandler.executeTask(taskSplit));
            }
            return list;
        });
    }

    private static <T> StreamObserver<TaskSplitResult<T>> getTaskSplitResultStreamObserver(List<TaskSplit<T>> taskSplits, CompletableFuture<List<TaskSplitResult<T>>> future) {
        List<TaskSplitResult<T>> results = new CopyOnWriteArrayList<>();
        AtomicInteger pendingTasks = new AtomicInteger(taskSplits.size());

        // 获取流式任务处理器
        return new StreamObserver<TaskSplitResult<T>>() {
            @Override
            public void onNext(TaskSplitResult<T> result) {
                results.add(result);
            }

            @Override
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }

            @Override
            public void onCompleted() {
                if (pendingTasks.decrementAndGet() == 0) {
                    future.complete(results);
                }
            }
        };
    }

    private void initTaskSplitHandlerReferenceConfig() {
        ReferenceConfig<ITaskSplitHandler> reference = new ReferenceConfig<>();
        reference.setInterface(ITaskSplitHandler.class);
        reference.setGeneric("true");

        taskSplitHandler = reference.get();
        System.out.println();
    }

    public TaskStatus getStatus(String endpoint, String taskId) {
        if (endpoint == null || taskId == null) {
            return TaskStatus.FAILED;
        }
        return scheduler.getTaskStatus(taskId);
    }

    private static String generateTaskId() {
        return "task-" + System.currentTimeMillis() + "-" +
                Math.abs(System.nanoTime() % 10000);
    }
}