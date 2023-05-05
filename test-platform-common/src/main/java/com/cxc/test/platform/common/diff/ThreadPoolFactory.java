package com.cxc.test.platform.common.diff;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolFactory {

    private static ExecutorService dataCheckExecutorService = null;

    private static ExecutorService dataCheckTimerExecutorService = null;

    private static ExecutorService apiCheckExecutorService = null;

    private static ExecutorService apiCheckTimerExecutorService = null;

    private static ExecutorService executorService = null;


    public static ExecutorService getDataCheckExecutorService() {
        if (dataCheckExecutorService == null) {
            dataCheckExecutorService = Executors.newFixedThreadPool(100);
        }

        return dataCheckExecutorService;
    }


    public static ExecutorService getDataCheckTimerExecutorService() {
        if (dataCheckTimerExecutorService == null) {
            dataCheckTimerExecutorService = Executors.newSingleThreadExecutor();
        }

        return dataCheckTimerExecutorService;
    }


    public static ExecutorService getApiCheckExecutorService() {
        if (apiCheckExecutorService == null) {
            apiCheckExecutorService = Executors.newFixedThreadPool(100);
        }

        return apiCheckExecutorService;
    }


    public static ExecutorService getApiCheckTimerExecutorService() {
        if (apiCheckTimerExecutorService == null) {
            apiCheckTimerExecutorService = Executors.newSingleThreadExecutor();
        }

        return apiCheckTimerExecutorService;
    }

    // 默认3线程
    public static ExecutorService getGeneralExecutorService() {
        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(3);
        }

        return executorService;
    }
}
