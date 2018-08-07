package ru.ifmo.rain.ugay.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;


public class ParallelMapperImpl implements ParallelMapper {
    class MyInt {
        private int val;

        MyInt() {
            val = 0;
        }

        synchronized void inc() {
            val++;
        }

        int get() {
            return val;
        }
    }

    class StreamWorker<T, R> implements Runnable {
        private T x;
        private Function<? super T, ? extends R> function;
        private final MyInt val;
        R ans = null;

        StreamWorker(T x, Function<? super T, ? extends R> f, MyInt val) {
            this.val = val;
            this.x = x;
            function = f;
        }

        private void add() {
            synchronized (val) {
                val.inc();
                val.notify();
            }
        }

        @Override
        public void run() {
            if (!Thread.currentThread().isInterrupted()) {
                ans = function.apply(x);
                add();
            }
        }
    }

    private class Worker extends Thread {
        public void run() {
            Runnable r;
            while (true) {
                synchronized (queue) {
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        } catch (InterruptedException interrupt) {
                            return;
                        }
                    }
                    r = queue.remove();
                }
                try {
                    r.run();
                } catch (RuntimeException e) {
                    System.err.println("Error while applying the function");
                }
            }
        }
    }

    private Thread[] th;
    private final Queue<Runnable> queue;

    public ParallelMapperImpl(int threads) {
        th = new Thread[threads];
        queue = new LinkedList<>();
        for (int i = 0; i < th.length; i++) {
            th[i] = new Worker();
            th[i].start();
        }
    }

    private void execute(Runnable r) {
        synchronized (queue) {
            queue.add(r);
            queue.notify();
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        List<StreamWorker<T, R>> sworkers = new ArrayList<>();
        final MyInt val = new MyInt();
        for (int i = 0; i < args.size(); i++) {
            sworkers.add(new StreamWorker<>(args.get(i), f, val));
            execute(sworkers.get(i));
        }
        synchronized (val) {
            while (val.get() < args.size()) {
                val.wait();
            }
        }
        List<R> myRes = new ArrayList<>();
        for (StreamWorker<T, R> sworker : sworkers) {
            myRes.add(sworker.ans);
        }
        return myRes;
    }

    @Override
    public void close() {
        for (Thread aTh : th) {
            aTh.interrupt();
        }
        try {
            for (Thread aTh : th) {
                aTh.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
