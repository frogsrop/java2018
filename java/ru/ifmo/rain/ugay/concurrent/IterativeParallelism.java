package ru.ifmo.rain.ugay.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterativeParallelism implements ListIP {

    private ParallelMapper pm;

    public IterativeParallelism() {
    }

    public IterativeParallelism(ParallelMapper pm) {
        this.pm = pm;
    }

    class StreamWorker<X, T> implements Runnable {

        private Stream<? extends T> x;
        private Function<Stream<? extends T>, X> function;
        X ans = null;

        StreamWorker(Stream<? extends T> x, Function<Stream<? extends T>, X> f) {
            this.x = x;
            function = f;
        }

        @Override
        public void run() {
            ans = function.apply(x);
        }
    }

    private <T> List<Stream<? extends T>> parser(int threads, List<? extends T> values) {
        int from = 0;
        int step = values.size() / threads;
        List<Stream<? extends T>> st = new ArrayList<>(threads);
        int i;
        for (i = 0; i < threads - 1; i++) {
            st.add(values.subList(from, from + step).stream());
            from += step;
        }
        st.add(values.subList(from, values.size()).stream());
        return st;
    }

    private <X, T> List<X> threadRunner(int threads, List<? extends T> values, Function<Stream<? extends T>, X> f) {
        List<Stream<? extends T>> st = parser(threads, values);
        if (pm != null) {
            try {
                return pm.map(f, st);
            } catch (InterruptedException ex)
            {
            }
        } else {
            ArrayList<StreamWorker<X, ? extends T>> arr = new ArrayList<>();
            for (Stream<? extends T> aSt : st) {
                arr.add(new StreamWorker<>(aSt, f));
            }
            Thread[] thr = new Thread[threads];
            for (int i = 0; i < threads; i++) {
                thr[i] = new Thread(arr.get(i));
            }
            for (int i = 0; i < threads; i++) {
                thr[i].start();
            }
            try {
                for (int i = 0; i < threads; i++) {
                    thr[i].join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            List<X> myRes = new ArrayList<>(threads);
            for (int i = 0; i < threads; i++) {
                myRes.add(arr.get(i).ans);
            }
            return myRes;
        }
        return null;
    }

    private <T, X> List<X> getMyRes(int threads, List<? extends T> values, Function<Stream<? extends T>, X> f) {
        threads = Math.min(threads, values.size());
        return threadRunner(threads, values, f);
    }

    private <T, R> R makeEverything(int threads, List<? extends T> values, Function<Stream<? extends T>, R> f, BiFunction<R, R, R> joiner) {
        List<R> myRes = getMyRes(threads, values, f);
        R ans = myRes.get(0);
        for (int i = 1; i < myRes.size(); i++) {
            ans = joiner.apply(ans, myRes.get(i));
        }
        return ans;
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        StringBuilder ans = makeEverything(
                threads,
                map(threads, values, Object::toString),
                x -> x.reduce(new StringBuilder(), StringBuilder::append, StringBuilder::append),
                StringBuilder::append
        );
        return ans.toString();
    }

    static private <T> List<T> listsJoiner(List<T> x, List<T> y) {
        x.addAll(y);
        return x;
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return makeEverything(
                threads,
                values,
                (Stream<? extends T> x) -> x.filter(predicate).collect(Collectors.toList()),
                IterativeParallelism::listsJoiner
        );
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return makeEverything(threads, values, (Stream<? extends T> x) -> x.map(f).collect(Collectors.toList()), IterativeParallelism::listsJoiner);
    }

    static private <T> T getMin(T x, T y, Comparator<? super T> comp) {
        if (comp.compare(x, y) < 0) {
            return y;
        } else {
            return x;
        }
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return makeEverything(threads, values, (Stream<? extends T> x) -> x.max(comparator).get(), (T x, T y) -> getMin(x, y, comparator));
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return makeEverything(threads, values, (Stream<? extends T> x) -> x.allMatch(predicate), (Boolean x, Boolean y) -> x & y);
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return makeEverything(threads, values, (Stream<? extends T> x) -> x.anyMatch(predicate), (Boolean x, Boolean y) -> x | y);
    }

}
