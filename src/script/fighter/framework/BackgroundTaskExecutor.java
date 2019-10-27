package script.fighter.framework;


import com.acuitybotting.common.utils.ExecutorUtil;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class BackgroundTaskExecutor {

    private static ScheduledExecutorService executor;
    private static ScheduledFuture<?> task;

    private static ScheduledExecutorService getExecutor() {
        if(executor == null) {
            executor = ExecutorUtil.newScheduledExecutorPool(3, Throwable::printStackTrace);
        }
        return executor;
    }

    public static void submit(Runnable runnable, long loop) {
        task = getExecutor().scheduleAtFixedRate(runnable, 0, loop, TimeUnit.MILLISECONDS);
    }

    private static void shutdown() {
        getExecutor().shutdown();
    }

    private static void shutdownNow() {
        getExecutor().shutdownNow();
    }

    public static void dispose() {
        if (task != null) {
            task.cancel(true);
        }
        getExecutor().shutdown();
        getExecutor().shutdownNow();
    }

    public static boolean isDisposed() {
        return (isShutdown() && isTerminated()) && (task == null || task.isCancelled());
    }

    public static boolean isStarted() {
        return task == null;
    }

    private static boolean isShutdown() {
        return getExecutor().isShutdown();
    }

    private static boolean isTerminated() {
        return getExecutor().isTerminated();
    }
}
