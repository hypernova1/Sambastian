package org.sam.server;

import org.sam.server.common.ServerProperties;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolManager {

    public static ThreadPoolExecutor getThreadPoolExecutor() {
        String maximumPoolSizeValue = ServerProperties.get("server.maximum-pool-size");
        int maximumPoolSize = 200;
        if (maximumPoolSizeValue != null) {
            maximumPoolSize = Integer.parseInt(maximumPoolSizeValue);
        }

        return new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors(),
                maximumPoolSize,
                150L,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>()
        );
    }

}
