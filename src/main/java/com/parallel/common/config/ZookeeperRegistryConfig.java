package com.parallel.common.config;

public class ZookeeperRegistryConfig {
    private static final String DEFAULT_ZK_ADDRESS = "zookeeper://47.115.173.204:2881";
    
    public static String getZookeeperAddress() {
        // 优先级：系统属性 > 环境变量 > 默认值
        String address = System.getProperty("parallel.registry.address");
        if (address != null && !address.isEmpty()) {
            return address;
        }
        
        address = System.getenv("PARALLEL_REGISTRY_ADDRESS");
        if (address != null && !address.isEmpty()) {
            return address;
        }
        
        return DEFAULT_ZK_ADDRESS;
    }
} 