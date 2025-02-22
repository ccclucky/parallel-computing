package team.cclucky.parallel.master;


import team.cclucky.parallel.core.Worker;
import team.cclucky.parallel.core.task.TaskSplit;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LoadBalancer {
    public <T> Map<Worker, List<TaskSplit<T>>> allocate(
            List<TaskSplit<T>> splits, 
            List<Worker> workers) {
        // 简单的轮询分配策略
        return splits.stream()
            .collect(Collectors.groupingBy(
                split -> workers.get(Math.abs(split.hashCode() % workers.size()))
            ));
    }
} 