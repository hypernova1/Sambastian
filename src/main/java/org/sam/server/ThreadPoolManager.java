package org.sam.server;

import org.sam.server.common.ServerProperties;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 요청 처리용 스레드풀을 생성하는 클래스
 *
 * @author hypernova1
 */
public class ThreadPoolManager {

    private static final int DEFAULT_MAXIMUM_POOL_SIZE = 200;

    private ThreadPoolManager() {}

    public static ThreadPoolExecutor getThreadPoolExecutor() {
        int maximumPoolSize = getMaximumPoolSize();

        // 무한 큐를 사용하면 corePoolSize를 넘는 스레드가 절대 생성되지 않아
        // maximumPoolSize 설정이 무효화되므로 SynchronousQueue로 직접 핸드오프한다.
        // 스레드가 모두 사용 중이면 CallerRunsPolicy로 accept 루프가 직접 처리해 백프레셔를 만든다.
        return new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors(),
                maximumPoolSize,
                60L,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                namedThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    private static int getMaximumPoolSize() {
        String maximumPoolSizeValue = ServerProperties.get("server.maximum-pool-size");
        if (maximumPoolSizeValue == null) {
            return DEFAULT_MAXIMUM_POOL_SIZE;
        }
        try {
            int maximumPoolSize = Integer.parseInt(maximumPoolSizeValue.trim());
            if (maximumPoolSize < 1) {
                return DEFAULT_MAXIMUM_POOL_SIZE;
            }
            return maximumPoolSize;
        } catch (NumberFormatException e) {
            return DEFAULT_MAXIMUM_POOL_SIZE;
        }
    }

    private static ThreadFactory namedThreadFactory() {
        AtomicInteger threadNumber = new AtomicInteger(1);
        return runnable -> {
            Thread thread = new Thread(runnable, "sambastian-worker-" + threadNumber.getAndIncrement());
            thread.setDaemon(false);
            return thread;
        };
    }

}
