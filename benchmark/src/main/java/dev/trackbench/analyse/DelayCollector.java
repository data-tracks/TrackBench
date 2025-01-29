package dev.trackbench.analyse;

import dev.trackbench.BenchmarkContext;
import dev.trackbench.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;


public class DelayCollector {
    Deque<Long> delays = new ConcurrentLinkedDeque<>();

    List<DelayWorker> workers = new ArrayList<>();


    public DelayCollector(BenchmarkContext context) {

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

        Pair<String, String> avg = new Pair<>("AvgDelay", String.valueOf(delays.stream().reduce(0L, Long::sum) / delays.size()));

        Pair<String, String> max = new Pair<>("AvgDelay", String.valueOf(delays.stream().max(Long::compareTo).orElseThrow()));

        Pair<String, String> min = new Pair<>("AvgDelay", String.valueOf(delays.stream().min(Long::compareTo).orElseThrow()));

        return List.of(avg, max, min);
    }


}
