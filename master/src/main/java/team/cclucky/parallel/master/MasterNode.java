package team.cclucky.parallel.master;

import org.apache.dubbo.config.*;
import team.cclucky.parallel.core.Master;
import team.cclucky.parallel.core.config.ZookeeperRegistryConfig;
import team.cclucky.parallel.core.registry.IRegistrationService;
import team.cclucky.parallel.core.registry.IWorkerRegistry;

import java.util.logging.Logger;

import static org.apache.dubbo.monitor.MonitorService.APPLICATION;

public class MasterNode extends Master {
    private static final Logger logger = Logger.getLogger(MasterNode.class.getName());
    
    private final WorkerRegistry workerRegistry;
    private final TaskScheduler taskScheduler;
    private final LoadBalancer loadBalancer;
    private final TaskStateManager taskStateManager;
    private final TaskSubmitter taskSubmitter;
    private ServiceConfig<IWorkerRegistry> serviceConfig;

    private ApplicationConfig application;
    private RegistryConfig registry;
    private ReferenceConfig<IRegistrationService> reference;
    private IRegistrationService registrationService;

    public MasterNode(int port) {
        super(port);
        this.workerRegistry = new WorkerRegistry();
        this.loadBalancer = new LoadBalancer();
        this.taskStateManager = new TaskStateManager();
        this.taskScheduler = new TaskScheduler(workerRegistry, loadBalancer, taskStateManager);
        this.taskSubmitter = new TaskSubmitter(taskScheduler);
    }
    
    public void start() {
        try {
            // 初始化应用配置
            initApplicationConfig("parallel-computing-worker");

            // 初始化注册中心
            initZkRegistry();

            // 暴露服务
            exportDubboService(application);
            logger.info("Master node started on port: " + getPort());
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

            if (reference == null) {
                initDubboRegistrationService();
                registrationService.shutdown();
                logger.info("Master node stopped");
            }
        } catch (Exception e) {
            logger.severe("Error stopping master node: " + e.getMessage());
        }
    }

    private void initApplicationConfig(String appName) {
        application = new ApplicationConfig();
        application.setName(appName);
    }

    private void initZkRegistry() {
        if (registry == null) {
            // 创建注册中心配置
            registry = new RegistryConfig();
            registry.setAddress(ZookeeperRegistryConfig.getZookeeperAddress());
            registry.setTimeout(40000);
            logger.info("Zookeeper registry started");
        }
    }

    @SuppressWarnings("deprecation")
    private void exportDubboService(ApplicationConfig application) {
        // 创建服务配置
        serviceConfig = new ServiceConfig<>();
        serviceConfig.setApplication(application);
        serviceConfig.setRegistry(registry);
        serviceConfig.setInterface(IWorkerRegistry.class);
        serviceConfig.setRef(new WorkerRegistry());
        serviceConfig.setProtocol(new ProtocolConfig("dubbo", 20880));
        serviceConfig.setTimeout(10000);
        // 导出服务
        serviceConfig.export();
        logger.info("RPC server started, service exported");
    }

    @SuppressWarnings("deprecation")
    private void initDubboRegistrationService() {
        // 创建应用配置
        ApplicationConfig application = new ApplicationConfig();
        application.setName("parallel-computing-worker");

        // 创建服务引用
        reference = new ReferenceConfig<>();
        reference.setApplication(application);
        reference.setRegistry(registry);
        reference.setInterface(IRegistrationService.class);
        reference.setTimeout(40000);
        registrationService = reference.get();
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