package team.cclucky.sdk.example;

import team.cclucky.parallel.core.task.*;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author cclucky
 */
public class DataProcessTask extends Task<List<String>> {
    
    public DataProcessTask() {
        super(new DataTaskTaskProcessor());
        setTaskName("DataProcessTask-" + UUID.randomUUID());
    }
} 