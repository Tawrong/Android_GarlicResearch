package com.example.garlicapp;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Executor_Mqtt {
    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void executeInBackground(Runnable runnable) {
        executor.execute(runnable);
    }
    public static void shutdown(){
        executor.shutdown();
    }
}
