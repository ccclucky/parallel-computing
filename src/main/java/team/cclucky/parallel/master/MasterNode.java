package team.cclucky.parallel.master;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.config.*;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.apache.dubbo.config.bootstrap.builders.ServiceBuilder;
import team.cclucky.parallel.core.Master;
import team.cclucky.parallel.core.config.ZookeeperRegistryConfig;
import team.cclucky.parallel.core.registry.IWorkerRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author cclucky
 */
public class MasterNode extends Master {
    private static final Logger logger = Logger.getLogger(MasterNode.class.getName());

    static final Map<String, RegistryConfig> registryMap = new HashMap<>();

    private RegistryConfig registry;

    public static DubboBootstrap dubboBootstrap;

    private final WorkerRegistry workerRegistry;
    private final TaskScheduler taskScheduler;
    private final LoadBalancer loadBalancer;
    private final TaskStateManager taskStateManager;
    private final TaskSubmitter taskSubmitter;
    private ServiceConfig<IWorkerRegistry> serviceConfig;

    private ApplicationConfig application;

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
            initApplicationConfig("parallel-computing-master");

            // 初始化注册中心
            initZkRegistry();

            // 暴露服务
            exportService();
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

            logger.info("Master node stopped");
        } catch (Exception e) {
            logger.severe("Error stopping master node: " + e.getMessage());
        }
    }

    private void initApplicationConfig(String appName) {
        application = new ApplicationConfig();
        application.setName(appName);
        application.setSerializeCheckStatus("DISABLE");
        application.setCheckSerializable(false);
    }

    private void initZkRegistry() {
        if (registry == null) {
            // 创建注册中心配置
            registry = new RegistryConfig();
            registry.setAddress(ZookeeperRegistryConfig.getZookeeperAddress());
            registry.setTimeout(40000);
            logger.info("Zookeeper registry started");

            registryMap.put(ZookeeperRegistryConfig.getZookeeperAddress(), registry);
        }
    }

    private void exportService() {
        ProtocolConfig protocolConfig = new ProtocolConfig();
        protocolConfig.setName(CommonConstants.TRIPLE); // 使用 Triple 协议
        protocolConfig.setPort(50051);
        protocolConfig.setSerialization("hessian2"); // 设置序列化方式，如 hessian2、kryo、fst、protobuf

        dubboBootstrap = DubboBootstrap.getInstance()
                .application(application)
                .registry(registry)
                .protocol(protocolConfig)
                .service(ServiceBuilder.newBuilder()
                        .interfaceClass(IWorkerRegistry.class)
                        .ref(new WorkerRegistry())
                        .generic("true")
                        .build())
                .start();
    }

    public TaskSubmitter getTaskSubmitter() {
        return taskSubmitter;
    }
} 