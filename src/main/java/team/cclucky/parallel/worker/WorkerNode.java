package team.cclucky.parallel.worker;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.apache.dubbo.config.bootstrap.builders.ServiceBuilder;
import team.cclucky.parallel.core.Worker;
import team.cclucky.parallel.core.config.ZookeeperRegistryConfig;
import team.cclucky.parallel.core.handle.ITaskSplitHandler;
import team.cclucky.parallel.core.model.WorkerStatus;
import team.cclucky.parallel.core.registry.IWorkerRegistry;
import team.cclucky.parallel.worker.heartbeat.HeartbeatManager;
import team.cclucky.parallel.worker.rpc.consumer.TaskSplitHandler;

import java.util.logging.Logger;

/**
 * @author cclucky
 */
public class WorkerNode extends Worker {
    private static final Logger logger = Logger.getLogger(WorkerNode.class.getName());
    private final HeartbeatManager heartbeatManager;

    private transient ApplicationConfig application;
    private transient RegistryConfig registry;

    private transient DubboBootstrap dubboBootstrap;

    public WorkerNode(String masterEndpoint) {
        super(masterEndpoint);
        this.heartbeatManager = new HeartbeatManager(this, masterEndpoint);
    }

    public void start() {
        try {

            // 初始化应用配置
            initApplicationConfig("parallel-computing-worker");

            // 初始化注册中心
            initZkRegistry();

            // 暴露服务
            exportService();

            // 注册到Master
            registerToMaster();

            // 启动心跳管理
//            heartbeatManager.start();

            logger.info("Worker node started, ID: " + this.workerId);

            dubboBootstrap.await();
        } catch (Exception e) {
            logger.severe("Failed to start worker node: " + e.getMessage());
            throw new RuntimeException("Worker node startup failed", e);
        }
    }

    public void stop() {
        try {
            heartbeatManager.stop();
            deregisterFromMaster();
            logger.info("Worker node stopped, ID: " + this.workerId);
        } catch (Exception e) {
            logger.severe("Error stopping worker node: " + e.getMessage());
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
            logger.info("Zookezeper registry started");
        }
    }

    private void registerToMaster() {
        ReferenceConfig<IWorkerRegistry> reference = new ReferenceConfig<>();
        reference.setInterface(IWorkerRegistry.class);
        reference.setGeneric("true");

        IWorkerRegistry workerRegistry = reference.get();
        // 注册worker
        workerRegistry.addWorker(this);
        logger.info("Successfully registered to master: " + workerId);
    }

    private void deregisterFromMaster() {
        try {
            if (workerRegistryService != null) {
                workerRegistryService.removeWorker(workerId);
                logger.info("Successfully deregistered from master: " + workerId);
            }
        } catch (Exception e) {
            logger.severe("Failed to deregister worker: " + e.getMessage());
        }
    }

    private void exportService() {
        ProtocolConfig protocolConfig = new ProtocolConfig();
        protocolConfig.setName(CommonConstants.TRIPLE);
        protocolConfig.setPort(50052);
        protocolConfig.setSerialization("hessian2");

        dubboBootstrap = DubboBootstrap.getInstance()
                .application(application)
                .registry(registry)
                .protocol(protocolConfig)
                .service(ServiceBuilder.newBuilder()
                        .interfaceClass(ITaskSplitHandler.class)
                        .ref(new TaskSplitHandler(this))
                        .build())
                .start();
        logger.info("Task split handler started, service exported");
    }

    public boolean isAvailable() {
        return status == WorkerStatus.IDLE || status == WorkerStatus.RUNNING;
    }
}