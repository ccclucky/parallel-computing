package com.parallel.worker;

import com.parallel.common.config.ZookeeperRegistryConfig;
import com.parallel.common.model.WorkerMetadata;
import com.parallel.common.model.WorkerStatus;
import com.parallel.registry.IRegistrationService;
import com.parallel.registry.RegistrationService;
import com.parallel.worker.executor.TaskExecutor;
import com.parallel.worker.heartbeat.HeartbeatManager;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;

public class WorkerNode {
    private static final Logger logger = Logger.getLogger(WorkerNode.class.getName());
    
    private final String workerId;
    private final String masterEndpoint;
    private final WorkerMetadata metadata;
    private final TaskExecutor taskExecutor;
    private final HeartbeatManager heartbeatManager;
    private volatile WorkerStatus status;

    private ReferenceConfig<RegistrationService> reference;
    private IRegistrationService registrationService;
    
    public WorkerNode(String masterEndpoint) {
        this.workerId = generateWorkerId();
        this.masterEndpoint = masterEndpoint;
        this.metadata = createWorkerMetadata();
        this.taskExecutor = new TaskExecutor(this);
        this.heartbeatManager = new HeartbeatManager(this, masterEndpoint);
        this.status = WorkerStatus.IDLE;
    }
    
    public void start() {
        try {
            // 1. 启动任务执行器
            taskExecutor.start();
            
            // 2. 向Master注册
            registerToMaster();
            
            // 3. 启动心跳管理
            heartbeatManager.start();
            
            logger.info("Worker node started, ID: " + workerId);
        } catch (Exception e) {
            logger.severe("Failed to start worker node: " + e.getMessage());
            throw new RuntimeException("Worker node startup failed", e);
        }
    }
    
    public void stop() {
        try {
            heartbeatManager.stop();
            taskExecutor.stop();
            deregisterFromMaster();
            logger.info("Worker node stopped, ID: " + workerId);
        } catch (Exception e) {
            logger.severe("Error stopping worker node: " + e.getMessage());
        }
    }
    
    // Getters
    public String getWorkerId() { return workerId; }
    public String getMasterEndpoint() { return masterEndpoint; }
    public WorkerMetadata getMetadata() { return metadata; }
    public WorkerStatus getStatus() { return status; }
    
    // Status management
    public void setStatus(WorkerStatus newStatus) {
        WorkerStatus oldStatus = this.status;
        this.status = newStatus;
        logger.info(String.format("Worker %s status changed: %s -> %s", 
            workerId, oldStatus, newStatus));
    }
    
    private String generateWorkerId() {
        return "worker-" + UUID.randomUUID().toString();
    }
    
    private WorkerMetadata createWorkerMetadata() {
        return new WorkerMetadata(
            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().maxMemory(),
            System.getProperty("os.name"),
            System.getProperty("java.version"),
            getHostName()
        );
    }
    
    private String getHostName() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    private void registerToMaster() {
        try {
            // 初始化Dubbo客户端
            initDubboClient();
            
            // 调用远程注册服务
            registrationService.registerWorker(workerId, masterEndpoint, metadata);
            logger.info("Successfully registered to master: " + workerId);
        } catch (Exception e) {
            logger.severe("Failed to register worker: " + e.getMessage());
            throw new RuntimeException("Worker registration failed", e);
        }
    }
    
    private void deregisterFromMaster() {
        try {
            if (registrationService != null) {
                registrationService.deregisterWorker(workerId);
                logger.info("Successfully deregistered from master: " + workerId);
                
                // 清理Dubbo资源
                if (reference != null) {
                    reference.destroy();
                }
            }
        } catch (Exception e) {
            logger.severe("Failed to deregister worker: " + e.getMessage());
        }
    }
    
    @SuppressWarnings("deprecation")
    private void initDubboClient() {
        // 创建应用配置
        ApplicationConfig application = new ApplicationConfig();
        application.setName("parallel-computing-worker");
        
        // 创建注册中心配置
        RegistryConfig registry = new RegistryConfig();
        registry.setAddress(ZookeeperRegistryConfig.getZookeeperAddress());
        
        // 创建服务引用
        reference = new ReferenceConfig<>();
        reference.setApplication(application);
        reference.setRegistry(registry);
        reference.setInterface(IRegistrationService.class);
        reference.setTimeout(40000);
        
        // 获取服务代理
        registrationService = reference.get();
    }
    
    public boolean isAvailable() {
        return status == WorkerStatus.IDLE || status == WorkerStatus.RUNNING;
    }
    
    public static void main(String[] args) {
        String masterEndpoint = "localhost:8080";
        if (args.length > 0) {
            masterEndpoint = args[0];
        }
        
        WorkerNode worker = new WorkerNode(masterEndpoint);
        Runtime.getRuntime().addShutdownHook(new Thread(worker::stop));
        worker.start();
    }
} 