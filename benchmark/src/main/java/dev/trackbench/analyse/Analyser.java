package dev.trackbench.analyse;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.trackbench.configuration.BenchmarkConfig;
import dev.trackbench.configuration.BenchmarkContext;
import dev.trackbench.configuration.workloads.Workload;
import dev.trackbench.display.Display;
import dev.trackbench.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Function;


public class Analyser {
    private final BenchmarkContext context;
    Queue<Long> delays = new ConcurrentLinkedQueue<>();
    Map<Long, Long> throughputs = new ConcurrentHashMap<>();


    public Analyser(BenchmarkContext context) {
        this.context = context;

    }

    public static void start(BenchmarkContext context) {
        Analyser analyser = new Analyser(context);
        Display.INSTANCE.line();

        for (Pair<String, String> pair : analyser.analyseLatency()) {
            Display.INSTANCE.info("{}: {}", pair.left(), pair.right());
        }

        Display.INSTANCE.line();

        for (Pair<String, String> pair : analyser.analyseThroughput()) {
            Display.INSTANCE.info("{}: {}", pair.left(), pair.right());
        }

        Display.INSTANCE.line();
    }

    private List<Pair<String, String>> analyseThroughput() {
        executeAnalysis( node -> {
            long sendTick = node.get( "tick" ).asLong();

            throughputs.putIfAbsent(sendTick, 0L);
            throughputs.put(sendTick, throughputs.get(sendTick) + 1);
        });

        return avgMedianMinMax(throughputs.values(), "data points", false);
    }

    public List<Pair<String, String>> analyseLatency() {
        executeAnalysis( node -> {
            long receivedTick = BenchmarkConfig.getArrivedTick(Objects.requireNonNull( node ));
            long sendTick = node.get( "tick" ).asLong();

            delays.add( receivedTick - sendTick );
        });

        return avgMedianMinMax(delays, "ticks", true);
    }

    private @NotNull List<Pair<String, String>> avgMedianMinMax(Collection<Long> unsorted, String unit, boolean isTicks) {
        if (unsorted.isEmpty()) {
            throw new IllegalArgumentException("Empty collection for analysis");
        }
        Function<Long, String> ending = val -> " " + unit + ( isTicks ? " " + context.tickToTime(val) : "");

        List<Long> sorted = unsorted.stream().sorted().toList();

        long avgTicks = sorted.stream().reduce(0L, Long::sum) / sorted.size();
        Pair<String, String> avg = new Pair<>("AvgThroughput", avgTicks + ending.apply(avgTicks) );

        long medianTicks = sorted.size() % 2 == 0
                ? (sorted.get(sorted.size() / 2) + sorted.get(sorted.size() / 2 + 1)) / 2
                : sorted.get(sorted.size() / 2 + 1);
        Pair<String, String> median = new Pair<>("MedianThroughput", medianTicks + ending.apply(avgTicks));

        long maxTicks = sorted.stream().max(Long::compareTo).orElseThrow();
        Pair<String, String> max = new Pair<>("MaxThroughput", maxTicks + ending.apply(avgTicks));

        long minTicks = sorted.stream().min(Long::compareTo).orElseThrow();
        Pair<String, String> min = new Pair<>("MinThroughput", minTicks + ending.apply(avgTicks));

        return List.of(avg, median, max, min);
    }

    private void executeAnalysis(Consumer<ObjectNode> consumer) {
        List<ResultWorker> workers = new ArrayList<>();

        for (Map.Entry<Integer, Workload> entry : context.getWorkloads().entrySet()) {
            workers.add(new ResultWorker(consumer, context.getConfig().getResultFile(entry.getValue().getName(), entry.getKey()), context, this));
        }

        workers.forEach(ResultWorker::start);

        try {
            for (ResultWorker w : workers) {
                w.join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
