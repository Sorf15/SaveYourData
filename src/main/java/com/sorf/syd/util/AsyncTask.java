package com.sorf.syd.util;

import com.sorf.syd.Main;
import com.sorf.syd.util.event.EventListener;
import com.sorf.syd.util.event.StopEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;

public class AsyncTask {

    private static AsyncTask instance = new AsyncTask();
    private ExecutorService executorService;

    private AsyncTask() {
        Logger.debug("Constructing AsyncTask!");
        Main.EVENT_BUS.register(this);
        this.executorService = Executors.newFixedThreadPool(10);
    }

    public static @NotNull AsyncTask getInstance() {
        return instance;
    }

    public void invoke(@NotNull Runnable runnable) {
        if (!executorService.isShutdown()) {
            executorService.execute(runnable);
        }
    }

    public boolean invokeWithResult(@NotNull FutureTask<Boolean> task) {
        if (!executorService.isShutdown()) {
            boolean result = false;
            try {
                executorService.submit(task);
                result = task.get(100, TimeUnit.SECONDS);
            } catch (TimeoutException | ExecutionException | InterruptedException e) {
                Logger.warn("Failed execution of future task with error: %s", e.getLocalizedMessage());
                e.printStackTrace();
            }
            return result;
        }
        return false;
    }

    @EventListener
    public void stop(StopEvent event) {
        this.executorService.shutdown();
        Logger.warn("AsyncTask is shutdown!");
    }

}
