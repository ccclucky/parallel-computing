package com.parallel.common.model;

import java.io.Serializable;

public class WorkerMetadata implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final int cpuCores;
    private final long totalMemory;
    private final String osName;
    private final String javaVersion;
    private final String hostName;
    
    public WorkerMetadata(int cpuCores, long totalMemory, String osName, 
                         String javaVersion, String hostName) {
        this.cpuCores = cpuCores;
        this.totalMemory = totalMemory;
        this.osName = osName;
        this.javaVersion = javaVersion;
        this.hostName = hostName;
    }
    
    // Getters
    public int getCpuCores() { return cpuCores; }
    public long getTotalMemory() { return totalMemory; }
    public String getOsName() { return osName; }
    public String getJavaVersion() { return javaVersion; }
    public String getHostName() { return hostName; }
} 