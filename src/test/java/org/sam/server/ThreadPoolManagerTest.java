package org.sam.server;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThreadPoolManagerTest {

    @Test
    @DisplayName("코어 수를 넘는 동시 작업이 들어오면 maximumPoolSize까지 스레드를 늘린다")
    void 코어_초과_스레드_확장() throws InterruptedException {
        ThreadPoolExecutor executor = ThreadPoolManager.getThreadPoolExecutor();
        int taskCount = Runtime.getRuntime().availableProcessors() + 4;
        CountDownLatch started = new CountDownLatch(taskCount);
        CountDownLatch release = new CountDownLatch(1);

        try {
            for (int i = 0; i < taskCount; i++) {
                executor.execute(() -> {
                    started.countDown();
                    try {
                        release.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }

            // 무한 큐 기반이던 기존 구현에서는 코어 수 이상 스레드가 늘지 않아 타임아웃된다
            assertTrue(started.await(5, TimeUnit.SECONDS));
            assertTrue(executor.getPoolSize() >= taskCount);
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("워커 스레드는 식별 가능한 이름을 가진다")
    void 스레드_이름() throws Exception {
        ThreadPoolExecutor executor = ThreadPoolManager.getThreadPoolExecutor();
        try {
            String threadName = executor.submit(() -> Thread.currentThread().getName()).get(5, TimeUnit.SECONDS);
            assertTrue(threadName.startsWith("sambastian-worker-"));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("설정이 없으면 maximumPoolSize 기본값은 200이다")
    void 기본_최대_풀_크기() {
        ThreadPoolExecutor executor = ThreadPoolManager.getThreadPoolExecutor();
        try {
            assertEquals(200, executor.getMaximumPoolSize());
        } finally {
            executor.shutdownNow();
        }
    }

}
