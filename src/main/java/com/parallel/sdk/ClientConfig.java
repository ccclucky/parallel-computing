package com.parallel.sdk;

import com.parallel.core.TaskConfig;

public class ClientConfig {
    private int maxRetries = 3;
    private long timeout = 30000;
    private int maxConcurrentTasks = 100;
    private String accessKey;
    private String secretKey;
    private boolean enableMetrics = true;
    private String metricsEndpoint;
    
    public TaskConfig toTaskConfig() {
        TaskConfig config = new TaskConfig();
        config.setMaxRetries(maxRetries);
        config.setTimeout(timeout);
        config.setProperty("maxConcurrentTasks", maxConcurrentTasks);
        config.setProperty("enableMetrics", enableMetrics);
        config.setProperty("metricsEndpoint", metricsEndpoint);
        return config;
    }
    
    // Builder模式构建配置
    public static class Builder {
        private ClientConfig config = new ClientConfig();
        
        public Builder withMaxRetries(int maxRetries) {
            config.maxRetries = maxRetries;
            return this;
        }
        
        public Builder withTimeout(long timeout) {
            config.timeout = timeout;
            return this;
        }
        
        public Builder withMaxConcurrentTasks(int maxConcurrentTasks) {
            config.maxConcurrentTasks = maxConcurrentTasks;
            return this;
        }
        
        public Builder withCredentials(String accessKey, String secretKey) {
            config.accessKey = accessKey;
            config.secretKey = secretKey;
            return this;
        }
        
        public Builder withMetrics(boolean enable, String endpoint) {
            config.enableMetrics = enable;
            config.metricsEndpoint = endpoint;
            return this;
        }
        
        public ClientConfig build() {
            return config;
        }
    }
    
    // Getter方法
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public long getTimeout() {
        return timeout;
    }
    
    public int getMaxConcurrentTasks() {
        return maxConcurrentTasks;
    }
    
    public String getAccessKey() {
        return accessKey;
    }
    
    public String getSecretKey() {
        return secretKey;
    }
} 