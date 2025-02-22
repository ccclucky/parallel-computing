package team.cclucky.parallel.worker;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.apache.dubbo.config.*;
import team.cclucky.parallel.core.Worker;
import team.cclucky.parallel.core.registry.IWorkerRegistry;
import team.cclucky.parallel.core.config.ZookeeperRegistryConfig;
import team.cclucky.parallel.core.model.WorkerStatus;
import team.cclucky.parallel.core.registry.IRegistrationService;
import team.cclucky.parallel.core.task.TaskSplit;
import team.cclucky.parallel.core.task.TaskSplitResult;
import team.cclucky.parallel.worker.executor.TaskSplitExecutor;
import team.cclucky.parallel.worker.heartbeat.HeartbeatManager;
import team.cclucky.parallel.worker.rpc.consumer.RegistrationService;

public class WorkerNode extends Worker {
    private static final Logger logger = Logger.getLogger(WorkerNode.class.getName());

    private final TaskSplitExecutor taskSplitExecutor;
    private final HeartbeatManager heartbeatManager;

    private ApplicationConfig application;
    private RegistryConfig registry;
    private ReferenceConfig<RegistrationService> reference;
    private ServiceConfig<IRegistrationService> serviceConfig;
    private IRegistrationService registrationService;

    public WorkerNode(String masterEndpoint) {
        super(masterEndpoint);
        this.taskSplitExecutor = new TaskSplitExecutor(this);
        this.heartbeatManager = new HeartbeatManager(this, masterEndpoint);
    }
    
    public void start() {
        try {
            // 1. 启动任务执行器
            taskSplitExecutor.start();
            
            // 2. 初始化应用配置
            initApplicationConfig("parallel-computing-worker");

            // 3. 初始化注册中心
            initZkRegistry();

            // 4. 暴露服务
            exportDubboService(application);

            // 5. 注册到Master
            registerToMaster(this);

            // 6. 启动心跳管理
            heartbeatManager.start();
            
            logger.info("Worker node started, ID: " + this.workerId);
        } catch (Exception e) {
            logger.severe("Failed to start worker node: " + e.getMessage());
            throw new RuntimeException("Worker node startup failed", e);
        }
    }
    
    public void stop() {
        try {
            heartbeatManager.stop();
            taskSplitExecutor.stop();
            deregisterFromMaster();
            logger.info("Worker node stopped, ID: " + this.workerId);
        } catch (Exception e) {
            logger.severe("Error stopping worker node: " + e.getMessage());
        }
    }

    private void initApplicationConfig(String appName) {
        ApplicationConfig application = new ApplicationConfig();
        application.setName(appName);
    }

    private void initZkRegistry() {
        if (registry == null) {
            // 创建注册中心配置
            registry = new RegistryConfig();
            registry.setAddress(ZookeeperRegistryConfig.getZookeeperAddress());
            logger.info("Zookeeper registry started");
        }
    }

    private void registerToMaster(Worker workerNode) {
        try {
            reference = new ReferenceConfig<>();
            reference.setRegistry(registry);
            reference.setInterface(IRegistrationService.class);
            reference.setRetries(3);  // 失败后重试 3 次
            reference.setTimeout(5000);  // 超时时间 5s

            registrationService = reference.get();
            
            // 调用远程注册服务
            registrationService.registerWorker(workerNode);
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
    private void exportDubboService(ApplicationConfig application) {
        // 创建服务配置
        serviceConfig = new ServiceConfig<>();
        serviceConfig.setApplication(application);
        serviceConfig.setRegistry(registry);
        serviceConfig.setInterface(IRegistrationService.class);
        serviceConfig.setRef(new RegistrationService(getWorkerRegistryService()));
        serviceConfig.setProtocol(new ProtocolConfig("dubbo", 20880));
        serviceConfig.setTimeout(10000);
        // 导出服务
        serviceConfig.export();
        logger.info("RPC server started, service exported");
    }

    private IWorkerRegistry getWorkerRegistryService() {
        // 创建引用配置
        ReferenceConfig<IWorkerRegistry> reference = new ReferenceConfig<>();
        reference.setInterface(IWorkerRegistry.class);
        reference.setRegistry(registry);
        reference.setRetries(3);  // 失败后重试 3 次
        reference.setTimeout(5000);  // 超时时间 5s

        // 获取远程服务
        return reference.get();
    }
    
    public boolean isAvailable() {
        return status == WorkerStatus.IDLE || status == WorkerStatus.RUNNING;
    }

    @Override
    public <T> List<TaskSplitResult<T>> executeTask(TaskSplit<T> taskSplit) {
        if (isAvailable()) {
            setStatus(WorkerStatus.RUNNING);
            taskSplitExecutor.executeTask(taskSplit);

        } else {
            logger.warning("Worker is not available for taskSplit execution: " + workerId);
        }

        return null;
    }
} 