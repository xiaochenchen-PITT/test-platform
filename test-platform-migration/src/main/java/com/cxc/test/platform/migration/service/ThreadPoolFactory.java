package com.cxc.test.platform.migration.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolFactory {

    private static ExecutorService executorService = null;

    /**
     * 默认100并发
     * @return
     */
    public static ExecutorService getExecutorService() {
        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(100);
        }

        return executorService;
    }
}
