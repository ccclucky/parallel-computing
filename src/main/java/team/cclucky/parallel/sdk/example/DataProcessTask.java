package team.cclucky.parallel.sdk.example;

import team.cclucky.parallel.core.task.Task;

import java.util.List;
import java.util.UUID;

/**
 * @author cclucky
 */
public class DataProcessTask extends Task<List<String>> {
    
    public DataProcessTask() {
        super(new DataTaskTaskProcessor());
        setTaskName("DataProcessTask-" + UUID.randomUUID());
    }
} 