package team.cclucky.parallel.core.task;

import java.io.Serializable;
import java.util.List;

/**
 * @author cclucky
 */
public interface TaskProcessor<T> extends Serializable {

    List<TaskSplit<T>> split(Task<T> input);

    TaskSplitResult<T> execute(TaskSplit<T> split);

    TaskResult<T> merge(List<TaskSplitResult<T>> results);
}

