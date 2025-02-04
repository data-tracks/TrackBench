package dev.trackbench.analyse;

import dev.trackbench.configuration.BenchmarkContext;
import dev.trackbench.configuration.workloads.Workload;
import dev.trackbench.util.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;


public class LatencyAnalyser {
    private final BenchmarkContext context;
    Queue<Long> delays = new ConcurrentLinkedQueue<>();

    List<DelayWorker> workers = new ArrayList<>();


    public LatencyAnalyser(BenchmarkContext context) {
        this.context = context;
        for (Map.Entry<Integer, Workload> entry : context.getWorkloads().entrySet()) {
            workers.add(new DelayWorker(context.getConfig().getResultFile(entry.getValue().getName(), entry.getKey()), context, this));
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
        List<Long> sortedDelays = delays.stream().sorted().toList();

        long avgTicks = delays.stream().reduce(0L, Long::sum) / delays.size();
        Pair<String, String> avg = new Pair<>("AvgLatency", avgTicks + " ticks " + context.tickToTime(avgTicks));

        long medianTicks = delays.size() % 2 == 0
                ? (sortedDelays.get(sortedDelays.size() / 2) + sortedDelays.get(sortedDelays.size() / 2 + 1)) / 2
                : sortedDelays.get(sortedDelays.size() / 2 + 1);
        Pair<String, String> median = new Pair<>("MedianLatency", medianTicks + " ms");

        long maxTicks = delays.stream().max(Long::compareTo).orElseThrow();
        Pair<String, String> max = new Pair<>("MaxLatency", maxTicks + " ticks " + context.tickToTime(maxTicks));

        long minTicks = delays.stream().min(Long::compareTo).orElseThrow();
        Pair<String, String> min = new Pair<>("MinLatency", minTicks + " ticks " + context.tickToTime(minTicks));

        return List.of(avg, median, max, min);
    }


}
