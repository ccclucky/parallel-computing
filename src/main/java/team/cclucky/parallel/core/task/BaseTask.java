package team.cclucky.parallel.core.task;

/**
 * @author cclucky
 */
public abstract class BaseTask<T> {
    private final TaskProcessor<T> processor;

    public BaseTask(TaskProcessor<T> processor) {
        this.processor = processor;
    }

    public TaskProcessor<T> getProcessor() {
        return processor;
    }
}
