package dev.trackbench.analyse;

import dev.trackbench.configuration.BenchmarkContext;
import dev.trackbench.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;


public class DelayCollector {
    private final BenchmarkContext context;
    Deque<Long> delays = new ConcurrentLinkedDeque<>();

    List<DelayWorker> workers = new ArrayList<>();


    public DelayCollector(BenchmarkContext context) {
        this.context = context;
        for (File file : context.getConfig().getResultFiles()) {
            workers.add(new DelayWorker(file, context, this));
        }
    }

    public List<Pair<String, String>> start() {
        workers.forEach(DelayWorker::start);

        try {
            for (DelayWorker w : workers) {
                w.join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        long avgTicks = delays.stream().reduce(0L, Long::sum) / delays.size();
        Pair<String, String> avg = new Pair<>("AvgDelay", avgTicks + " ticks " + context.tickToTime(avgTicks));

        long maxTicks = delays.stream().max(Long::compareTo).orElseThrow();
        Pair<String, String> max = new Pair<>("Max", maxTicks + " ticks " + context.tickToTime(maxTicks));

        long minTicks = delays.stream().min(Long::compareTo).orElseThrow();
        Pair<String, String> min = new Pair<>("Min", minTicks + " ticks " + context.tickToTime(minTicks));

        return List.of(avg, max, min);
    }


}
