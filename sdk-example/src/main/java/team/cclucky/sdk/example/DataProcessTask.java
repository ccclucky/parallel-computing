package team.cclucky.sdk.example;

import team.cclucky.parallel.core.task.*;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

public class DataProcessTask extends Task<List<String>> {
    
    private final int batchSize;
    
    public DataProcessTask() {
        this(1000); // 默认批次大小
    }
    
    public DataProcessTask(int batchSize) {
        this.batchSize = batchSize;
        setTaskName("DataProcessTask-" + UUID.randomUUID().toString());
    }
    
    @Override
    public List<TaskSplit<List<String>>> split(List<String> input) {
        List<TaskSplit<List<String>>> splits = new ArrayList<>();
        
        for (int i = 0; i < input.size(); i += batchSize) {
            TaskSplit<List<String>> split = new TaskSplit<>();
            split.setTaskId(getTaskId());
            split.setSplitId(getTaskId() + "-" + (i / batchSize));
            split.setData(input.subList(i, Math.min(i + batchSize, input.size())));
            split.setStatus(TaskSplitStatus.PENDING);
            split.setPartitionNum(i / batchSize);
            split.setDataType(this.getClass());
            split.setFunction(this::execute);
            splits.add(split);
        }
        
        return splits;
    }
    
    @Override
    public TaskSplitResult<List<String>> execute(TaskSplit<List<String>> split) {
        List<String> processed = split.getData().stream()
            .map(this::processItem)
            .collect(Collectors.toList());
            
        TaskSplitResult<List<String>> result = new TaskSplitResult<>();
        result.setTaskId(getTaskId());
        result.setSplitId(split.getSplitId());
        result.setResult(processed);
        result.setStatus(TaskSplitStatus.COMPLETED);
        result.setStartTime(System.currentTimeMillis());
        result.setEndTime(System.currentTimeMillis());
        
        return result;
    }
    
    @Override
    public TaskResult<List<String>> merge(List<TaskSplitResult<List<String>>> results) {
        List<String> merged = results.stream()
            .flatMap(r -> r.getResult().stream())
            .collect(Collectors.toList());
            
        TaskResult<List<String>> finalResult = new TaskResult<>();
        finalResult.setTaskId(getTaskId());
        finalResult.setResult(merged);
        finalResult.setStatus(TaskStatus.COMPLETED);
        
        return finalResult;
    }
    
    private String processItem(String item) {
        // 示例处理逻辑
        return item.toUpperCase();
    }
} 