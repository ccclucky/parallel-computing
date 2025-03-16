package team.cclucky.sdk.example;

import team.cclucky.parallel.core.task.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cclucky
 */
public class DataTaskTaskProcessor implements TaskProcessor<List<String>> {

    private final int batchSize;

    public DataTaskTaskProcessor() {
        this(1000);
    }

    public DataTaskTaskProcessor(int batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public List<TaskSplit<List<String>>> split(Task<List<String>> task) {
        List<TaskSplit<List<String>>> splits = new ArrayList<>();
        List<String> input = task.getInput();
        for (int i = 0; i < input.size(); i += batchSize) {
            TaskSplit<List<String>> split = new TaskSplit<>(this);
            split.setTaskId(task.getTaskId());
            split.setSplitId(task.getTaskId() + "-" + (i / batchSize));
            split.setData(input.subList(i, Math.min(i + batchSize, input.size())));
            split.setStatus(TaskSplitStatus.PENDING);
            split.setPartitionNum(i / batchSize);
            split.setDataType(this.getClass());
            splits.add(split);
        }

        return splits;
    }

    @Override
    public TaskSplitResult<List<String>> execute(TaskSplit<List<String>> split) {
        List<String> processed = split.getData().stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        TaskSplitResult<List<String>> result = new TaskSplitResult<>();
        result.setTaskId(split.getTaskId());
        result.setSplitId(split.getSplitId());
        result.setResult(processed);
        result.setStatus(TaskSplitStatus.COMPLETED);
        result.setStartTime(System.currentTimeMillis());
        result.setEndTime(System.currentTimeMillis());

        return result;
    }

    @Override
    public TaskResult<List<String>> merge(List<TaskSplitResult<List<String>>> taskSplitResults) {
        List<String> merged = taskSplitResults.stream()
                .flatMap(r -> r.getResult().stream())
                .collect(Collectors.toList());
        TaskResult<List<String>> finalResult = new TaskResult<>();
        finalResult.setTaskId(taskSplitResults.get(0).getTaskId());
        finalResult.setResult(merged);
        finalResult.setStatus(TaskStatus.COMPLETED);

        return finalResult;
    }
}
