package com.parallel.worker.executor;

import com.parallel.core.Task;
import com.parallel.core.TaskResult;
import com.parallel.worker.TaskExceptionHandler;
import com.parallel.worker.WorkerNode;
import com.parallel.common.model.WorkerStatus;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class TaskExecutor {
    private static final Logger logger = Logger.getLogger(TaskExecutor.class.getName());
    
    private final WorkerNode worker;
    private final ExecutorService executorService;
    private volatile boolean running;
    
    public TaskExecutor(WorkerNode worker) {
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
    
    public <T> void executeTask(Task<T> task) {
        if (!running) {
            throw new IllegalStateException("TaskExecutor is not running");
        }
        
        worker.setStatus(WorkerStatus.RUNNING);
        
        executorService.submit(() -> {
            try {
                TaskResult<T> result = task.execute(task.split(task.getInput()).get(0));
                handleTaskResult(result);  // 处理任务结果
                worker.setStatus(WorkerStatus.IDLE);
            } catch (Exception e) {
                worker.setStatus(WorkerStatus.ERROR);
                TaskExceptionHandler.handleTaskException(task.split(task.getInput()).get(0), task, e);
            }
        });
    }
    
    private <T> void handleTaskResult(TaskResult<T> result) {
        // TODO: 将结果返回给Master
        logger.info("Task completed with result: " + result);
    }
    
    public boolean isRunning() {
        return running;
    }
} 