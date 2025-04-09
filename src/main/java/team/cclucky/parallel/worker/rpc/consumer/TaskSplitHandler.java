package team.cclucky.parallel.worker.rpc.consumer;

import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.cclucky.parallel.core.handle.ITaskSplitHandler;
import team.cclucky.parallel.core.model.WorkerStatus;
import team.cclucky.parallel.core.task.TaskSplit;
import team.cclucky.parallel.core.task.TaskSplitResult;
import team.cclucky.parallel.core.task.TaskSplitStatus;
import team.cclucky.parallel.worker.WorkerNode;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author cclucky
 */
@DubboService
public class TaskSplitHandler implements ITaskSplitHandler {
    private static final Logger logger = LoggerFactory.getLogger(TaskSplitHandler.class);

    private final WorkerNode worker;
    private final ExecutorService executorService;

    public TaskSplitHandler(WorkerNode worker) {
        this.worker = worker;
        this.executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors()
        );
    }

    @Override
    public <T> TaskSplitResult<T> executeTask(TaskSplit<T> taskSplit) {
        logger.info("Executing task split: {}", taskSplit.getSplitId());
        
        try {
            worker.setStatus(WorkerStatus.RUNNING);
            taskSplit.setStatus(TaskSplitStatus.RUNNING);
            
            // 直接执行任务分片的处理函数
            TaskSplitResult<T> result = taskSplit.getProcessor().execute(taskSplit);

            if (result == null) {
                throw new RuntimeException("Task execution returned null result");
            }
            
            taskSplit.setStatus(TaskSplitStatus.COMPLETED);
            logger.info("Task split completed: {}", taskSplit.getSplitId());
            return result;
            
        } catch (Exception e) {
            logger.error("Task execution failed: ", e);
            taskSplit.setStatus(TaskSplitStatus.FAILED);
            throw new CompletionException("Task execution failed", e);
        } finally {
            worker.setStatus(WorkerStatus.IDLE);
        }
    }

    public String test(String s) {
        return "test";
    }
}
