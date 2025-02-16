package com.parallel.master;

import com.parallel.core.TaskSplit;
import com.parallel.worker.WorkerNode;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LoadBalancer {
    public <T> Map<WorkerNode, List<TaskSplit<T>>> allocate(
            List<TaskSplit<T>> splits, 
            List<WorkerNode> workers) {
        // 简单的轮询分配策略
        return splits.stream()
            .collect(Collectors.groupingBy(
                split -> workers.get(Math.abs(split.hashCode() % workers.size()))
            ));
    }
} 