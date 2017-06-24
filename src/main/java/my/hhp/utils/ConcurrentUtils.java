package my.hhp.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ConcurrentUtils {
    public static void stop(ExecutorService executor) {
        stop(executor, 5, TimeUnit.SECONDS);
    }

    public static void stop(ExecutorService executor, int waitPeriod, TimeUnit units) {
        try {
            System.out.println("attempt to shutdown executor");
            executor.shutdown();
            executor.awaitTermination(waitPeriod, units);
        } catch (InterruptedException e) {
            System.err.println("tasks interrupted");
        } finally {
            if (!executor.isTerminated()) {
                System.err.println("cancel non-finished tasks");
            }
            executor.shutdownNow();
            System.out.println("shutdown finished");
        }
    }

    public static void sleep(long millis) {
        try {
            MILLISECONDS.sleep(millis);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
