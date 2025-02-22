package team.cclucky.parallel.core;

import java.util.logging.Logger;

public class Master {
    private static final Logger logger = Logger.getLogger(Master.class.getName());

    private final int port;

    public Master(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
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
