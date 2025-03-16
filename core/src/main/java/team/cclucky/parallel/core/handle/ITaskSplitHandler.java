package team.cclucky.parallel.core.handle;

import team.cclucky.parallel.core.task.TaskSplit;
import team.cclucky.parallel.core.task.TaskSplitResult;

import java.util.concurrent.CompletableFuture;

/**
 * @author cclucky
 */
public interface ITaskSplitHandler {
    <T> TaskSplitResult<T> executeTask(TaskSplit<T> taskSplit);
}
