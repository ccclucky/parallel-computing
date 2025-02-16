package com.parallel.sdk.example;

import com.parallel.core.*;
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
            split.setSplitId(getTaskId() + "-" + (i / batchSize));
            split.setData(input.subList(i, Math.min(i + batchSize, input.size())));
            split.setPartitionNum(i / batchSize);
            splits.add(split);
        }
        
        return splits;
    }
    
    @Override
    public TaskResult<List<String>> execute(TaskSplit<List<String>> split) {
        List<String> processed = split.getData().stream()
            .map(this::processItem)
            .collect(Collectors.toList());
            
        TaskResult<List<String>> result = new TaskResult<>();
        result.setTaskId(getTaskId());
        result.setSplitId(split.getSplitId());
        result.setResult(processed);
        result.setStatus(TaskStatus.COMPLETED);
        result.setStartTime(System.currentTimeMillis());
        result.setEndTime(System.currentTimeMillis());
        
        return result;
    }
    
    @Override
    public TaskResult<List<String>> merge(List<TaskResult<List<String>>> results) {
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