package my.hhp.pingpong;

import java.lang.Thread;
import java.lang.Runnable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static my.hhp.pingpong.SemaphoredPingPonger.PingPong.*;
import static my.hhp.utils.ConcurrentUtils.*;

public class SemaphoredPingPonger {
    public static void main(String[] args) {
        Runnable pinger = () -> {
            for (int i = 0; i++ < 10; ) {
                println(i);
                ping();
                sleep(500);
            }
        };

        Runnable interrupted = () -> {
            for (int i = 0; i++ < 5; ) {
                ping();
                println("interrupting");
                Thread.currentThread().interrupt();
            }
        };

        Runnable broken = () -> {
            for (int i = 0; i++ < 5; ) {
                brokenPing();
            }
        };

        Runnable ponger = () -> {
//            Thread.currentThread().setName("ponger");
            for (int i = 0; i++ < 5; ) {
                pong();
//                Thread.currentThread().interrupt();
                sleep(10);
            }
        };

        ExecutorService executor = Executors.newCachedThreadPool();
        executor.submit(broken);
        executor.submit(ponger);
        executor.submit(interrupted);
        executor.submit(ponger);
        sleep(1000);
        executor.submit(pinger);
        executor.submit(pinger);
        sleep(100);
        executor.shutdownNow();
        stop(executor, 5, TimeUnit.SECONDS);
    }

    private static void println(Object value) {
//        System.out.println(makePrintString(value));
    }

    private static void print(Object value) {
        System.out.print(/*makePrintString(value)*/value + " ");
    }

    private static String makePrintString(Object value) {
        return value + " by " + Thread.currentThread().getName();
    }

    static class PingPong {
        private static final Semaphore ping = new Semaphore(1);
        private static final Semaphore pong = new Semaphore(1);

        static {
            try {
                pong.acquire();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public static void pong() {
            play(pong, false);
        }

        public static void ping() {
            ping(false);
        }

        public static void ping(boolean throwError) {
            play(ping, throwError);
        }

        public static void play(Semaphore next, boolean throwError) {
            boolean completed = false;
            boolean acquired = false;
            try {
                next.acquire();
                acquired = true;
                if (throwError) {
                    throw new Exception("куку");
                }
                print(next == ping ? "iii" : "ooo");
                completed = true;
            } catch (Exception e) {
                if ((e instanceof InterruptedException)) {
                    println("interrupted");
                } else {
                    throw new RuntimeException(e);
                }
            } finally {
                String who = next == ping ? "ping" : "pong";
                if (completed) {
                    String whom = next == ping ? "PONG" : "PING";
                    Semaphore release = next == ping ? pong : ping;
                    release.release();
                    println(who + " released " + whom);
                } else if (acquired) {
                    next.release();
                    println(who + " self released");
                }
            }
        }

        public static void brokenPing() {
            ping(true);
        }
    }
}

