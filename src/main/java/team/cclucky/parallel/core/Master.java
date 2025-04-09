package team.cclucky.parallel.core;

import java.util.logging.Logger;

/**
 * @author cclucky
 */
public class Master {
    private static final Logger logger = Logger.getLogger(Master.class.getName());

    private final int port;

    public Master(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }
}
