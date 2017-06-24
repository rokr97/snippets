package my.hhp.pingpong;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static my.hhp.pingpong.ConditionedPingPong.PingPongValue.*;
import static my.hhp.utils.ConcurrentUtils.*;

public class ConditionedPingPong {
    public static void main(String[] args) {
        int amount = 100;
        Runnable pinger = () -> {
            PingPong pingPong = new PingPong();
            for (int i = 0; i++ < amount; ) {
                pingPong.ping();
//                sleep(500);
            }
        };

        Runnable ponger = () -> {
            PingPong pingPong = new PingPong();
            for (int i = 0; i++ < amount / 2; ) {
                pingPong.pong();
                Thread.currentThread().interrupt();
//                sleep(1000);
            }
        };

        ExecutorService executor = Executors.newCachedThreadPool();
        executor.submit(ponger);
        executor.submit(ponger);
//        sleep(100);
        executor.submit(pinger);
        executor.submit(pinger);
//        sleep(100);

        stop(executor);
    }

    enum PingPongValue {PING, PONG}

    static class PingPong {
        private static Lock lock = new ReentrantLock();
        private static Condition ping = lock.newCondition();
        private static Condition pong = lock.newCondition();
        private static PingPongValue lastValue = PONG;

        public void pong() {
            play(PONG);
        }

        public void ping() {
            play(PING);
        }

        private void play(PingPongValue nextValue) {
            lock.lock();
            final Condition await = nextValue == PING ? pong : ping;
            final Condition signal = nextValue == PING ? ping : pong;

            try {
                while (lastValue == nextValue) {
                    await.await();
                }
                print(nextValue.name());
                lastValue = nextValue;
                signal.signalAll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }

        private static void print(String value) {
            System.out.println(value + " by " + Thread.currentThread().getName());
        }
    }
}
