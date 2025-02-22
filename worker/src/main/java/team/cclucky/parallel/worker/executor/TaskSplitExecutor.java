package team.cclucky.parallel.worker.executor;

import team.cclucky.parallel.core.task.TaskSplit;
import team.cclucky.parallel.core.task.TaskSplitResult;
import team.cclucky.parallel.core.task.TaskSplitStatus;
import team.cclucky.parallel.core.model.WorkerStatus;
import team.cclucky.parallel.worker.TaskExceptionHandler;
import team.cclucky.parallel.worker.WorkerNode;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class TaskSplitExecutor {
    private static final Logger logger = Logger.getLogger(TaskSplitExecutor.class.getName());
    
    private final WorkerNode worker;
    private final ExecutorService executorService;
    private volatile boolean running;
    
    public TaskSplitExecutor(WorkerNode worker) {
        this.worker = worker;
        this.executorService = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
        );
        this.running = false;
    }
    
    public void start() {
        running = true;
        worker.setStatus(WorkerStatus.IDLE);
        logger.info("TaskExecutor started for worker: " + worker.getWorkerId());
    }
    
    public void stop() {
        running = false;
        executorService.shutdown();
        worker.setStatus(WorkerStatus.STOPPED);
        logger.info("TaskExecutor stopped for worker: " + worker.getWorkerId());
    }
    
    public <T> Future<?> executeTask(TaskSplit<T> taskSplit) {
        if (!running) {
            throw new IllegalStateException("TaskExecutor is not running");
        }
        
        worker.setStatus(WorkerStatus.RUNNING);

        return executorService.submit(() -> {
            try {
                taskSplit.setStatus(TaskSplitStatus.RUNNING);
                TaskSplitResult<T> result = taskSplit.getFunction().apply(taskSplit);
                handleTaskResult(result);  // 处理任务结果
                worker.setStatus(WorkerStatus.IDLE);
                taskSplit.setStatus(TaskSplitStatus.COMPLETED);
            } catch (Exception e) {
                taskSplit.setStatus(TaskSplitStatus.FAILED);
                TaskExceptionHandler.handleTaskException(taskSplit, e);
            }
        });
    }

    private <T> void handleTaskResult(TaskSplitResult<T> result) {
        // TODO: 将结果返回给Master
        logger.info("Task completed with result: " + result);
        if (result.getStatus() == TaskSplitStatus.COMPLETED) {
            worker.setStatus(WorkerStatus.IDLE);
        }
    }
    
    public boolean isRunning() {
        return running;
    }
} 