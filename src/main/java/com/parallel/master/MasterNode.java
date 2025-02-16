package com.parallel.master;

import com.parallel.common.config.ZookeeperRegistryConfig;
import com.parallel.registry.IRegistrationService;
import com.parallel.registry.RegistrationService;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;

import java.util.logging.Logger;

public class MasterNode {
    private static final Logger logger = Logger.getLogger(MasterNode.class.getName());
    
    private final int port;
    private final WorkerRegistry workerRegistry;
    private final RegistrationService registrationService;
    private final TaskScheduler taskScheduler;
    private final LoadBalancer loadBalancer;
    private final TaskStateManager taskStateManager;
    private final TaskSubmitter taskSubmitter;
    private ServiceConfig<RegistrationService> serviceConfig;
    
    public MasterNode(int port) {
        this.port = port;
        this.workerRegistry = new WorkerRegistry();
        this.loadBalancer = new LoadBalancer();
        this.taskStateManager = new TaskStateManager();
        this.registrationService = new RegistrationService(workerRegistry);
        this.taskScheduler = new TaskScheduler(workerRegistry, loadBalancer, taskStateManager);
        this.taskSubmitter = new TaskSubmitter(taskScheduler);
    }
    
    public void start() {
        try {
            startRpcServer();
            logger.info("Master node started on port: " + port);
        } catch (Exception e) {
            logger.severe("Failed to start master node: " + e.getMessage());
            throw new RuntimeException("Master node startup failed", e);
        }
    }
    
    public void stop() {
        try {
            if (serviceConfig != null) {
                serviceConfig.unexport();
            }
            registrationService.shutdown();
            logger.info("Master node stopped");
        } catch (Exception e) {
            logger.severe("Error stopping master node: " + e.getMessage());
        }
    }
    
    @SuppressWarnings("deprecation")
    private void startRpcServer() {
        // 创建应用配置
        ApplicationConfig application = new ApplicationConfig();
        application.setName("parallel-computing-master");
        
        // 创建注册中心配置
        RegistryConfig registry = new RegistryConfig();
        registry.setAddress(ZookeeperRegistryConfig.getZookeeperAddress());
        registry.setTimeout(40000);
        
        // 创建服务配置
        serviceConfig = new ServiceConfig<>();
        serviceConfig.setApplication(application);
        serviceConfig.setRegistry(registry);
        serviceConfig.setInterface(IRegistrationService.class);
        serviceConfig.setRef(new RegistrationService(this.workerRegistry));
        serviceConfig.setProtocol(new ProtocolConfig("dubbo", 20880));
        
        // 导出服务
        serviceConfig.export();
        logger.info("RPC server started, service exported");
    }
    
    public TaskSubmitter getTaskSubmitter() {
        return taskSubmitter;
    }
    
//    public static void main(String[] args) {
//        int port = 8080; // 默认端口
//        if (args.length > 0) {
//            port = Integer.parseInt(args[0]);
//        }
//
//        MasterNode master = new MasterNode(port);
//        Runtime.getRuntime().addShutdownHook(new Thread(master::stop));
//        master.start();
//    }
} 