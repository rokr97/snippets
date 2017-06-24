package my.hhp.onecup;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class ZeroToOneHundredRealQuick {
    private static final int MAX_THREADS = 3;

    public static void main(String[] args) {
        new ZeroToOneHundredRealQuick(MAX_THREADS).runTo(100);
    }

    public ZeroToOneHundredRealQuick(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    private void runTo(int maxNumber) {
        Random random = new Random();
        Runnable task = () -> {
            String threadName = Thread.currentThread().getName();
            revRange(1, maxNumber + 1).forEach((num) -> {
                if (result.add(num)) {
                    System.out.println(threadName + " added " + num);
                    if (num == 1) {
//                        System.out.println(result.descendingSet());
                        System.out.println(result);
                    } else {
//                        Thread.yield();
                        try {
                            int timeout = random.nextInt(10);
                            TimeUnit.MICROSECONDS.sleep(timeout);
//                            System.out.println(threadName + " slept " + timeout);
                        } catch (InterruptedException e) {
                            throw new RuntimeException("InterruptedException caught in task", e);
                        }
                    }
                }
            });
        };

        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        IntStream.range(1, maxThreads + 1).forEach((i) -> {
            executor.submit(task);
        });
        executor.shutdown();
    }

    static IntStream revRange(int from, int to) {
        return IntStream.range(from, to).map(i -> to - i + from - 1);
    }

//    private final ConcurrentSkipListSet<Integer> result = new ConcurrentSkipListSet<>();
    private final Set<Integer> result = new CopyOnWriteArraySet<>();
    private final int maxThreads;
}


