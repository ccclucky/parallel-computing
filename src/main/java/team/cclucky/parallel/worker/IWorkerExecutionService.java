package team.cclucky.parallel.worker;


import team.cclucky.parallel.core.task.TaskContext;
import team.cclucky.parallel.core.task.TaskResult;
import team.cclucky.parallel.core.task.TaskSplit;

public interface IWorkerExecutionService {
    <T> TaskResult<T> execute(TaskSplit<T> split, TaskContext context);
} 