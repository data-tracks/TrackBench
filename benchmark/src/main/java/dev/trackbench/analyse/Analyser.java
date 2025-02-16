package dev.trackbench.analyse;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.trackbench.configuration.BenchmarkConfig;
import dev.trackbench.configuration.BenchmarkContext;
import dev.trackbench.configuration.workloads.Workload;
import dev.trackbench.display.Display;
import dev.trackbench.util.Pair;
import dev.trackbench.util.file.JsonSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;


public class Analyser {

    private final static Integer WORKERS = 32;

    private final BenchmarkContext context;
    private final Workload workload;
    Queue<Long> delays = new ConcurrentLinkedQueue<>();
    final Map<Long, Long> throughput_millis = new ConcurrentHashMap<>();
    final Map<Long, Long> throughput_seconds = new ConcurrentHashMap<>();


    public Analyser( BenchmarkContext context, Workload workload ) {
        this.context = context;
        this.workload = workload;

    }


    public static void start( BenchmarkContext context ) {
        for ( Map.Entry<Integer, Workload> entry : context.getWorkloads().entrySet() ) {
            analyseWorkload( context, entry.getValue() );
        }

    }


    private static void analyseWorkload( BenchmarkContext context, Workload workload ) {
        Analyser analyser = new Analyser( context, workload );
        Display.INSTANCE.line();
        Display.INSTANCE.bold( "Workload {} Results", workload.getName() );
        Display.INSTANCE.line();

        for ( Pair<String, String> pair : analyser.analyseLatency() ) {
            Display.INSTANCE.info( "{}: {}", pair.left(), pair.right() );
        }

        Display.INSTANCE.line();

        for ( Pair<String, String> pair : analyser.analyseThroughput() ) {
            Display.INSTANCE.info( "{}: {}", pair.left(), pair.right() );
        }

        Display.INSTANCE.line();
    }


    private List<Pair<String, String>> analyseThroughput() {
        long toMillis = 1_000_000 / context.getConfig().stepDurationNs();
        long toSecond = 1_000_000_000 / context.getConfig().stepDurationNs();
        long toMinute = 60_000_000_000L / context.getConfig().stepDurationNs();

        executeAnalysis( node -> {
            long milli = node.get( BenchmarkConfig.ARRIVED_TICK_KEY ).asLong() / toMillis; // we merge to millis
            long second = node.get( BenchmarkConfig.ARRIVED_TICK_KEY ).asLong() / toSecond; // we merge to seconds

            throughput_millis.putIfAbsent( milli, 0L );
            synchronized ( throughput_millis ) {
                throughput_millis.put( milli, throughput_millis.get( milli ) + 1 );
            }

            throughput_seconds.putIfAbsent( second, 0L );
            synchronized ( throughput_seconds ) {
                throughput_seconds.put( second, throughput_seconds.get( second ) + 1 );
            }

        } );

        return
                Stream.concat( avgMedianMinMax( throughput_millis.values(), "throughput", "data points per ms", false ).stream(),
                        avgMedianMinMax( throughput_seconds.values(), "throughput", "data points per s", false ).stream() ).toList();
    }


    public List<Pair<String, String>> analyseLatency() {
        executeAnalysis( node -> {
            long receivedTick = BenchmarkConfig.getArrivedTick( Objects.requireNonNull( node ) );
            long sendTick = node.get( "tick" ).asLong();

            delays.add( receivedTick - sendTick );
        } );

        return avgMedianMinMax( delays, "latency", "ticks", true );
    }


    private @NotNull List<Pair<String, String>> avgMedianMinMax( Collection<Long> unsorted, String name, String unit, boolean isTicks ) {
        if ( unsorted.isEmpty() ) {
            Display.INSTANCE.error( "Empty collection for analysis" );
            return List.of();
        }
        if ( unsorted.size() <= 2 ) {
            Display.INSTANCE.error( "Less than 2 elements in collection for analysis" );
            return List.of();
        }

        String capName = StringUtils.capitalize( name );
        Function<Long, String> ending = val -> " " + unit + (isTicks ? " " + context.tickToTime( val ) : "");

        List<Long> sorted = unsorted.stream().parallel().sorted().toList();

        long avgTicks = sorted.stream().reduce( 0L, Long::sum ) / sorted.size();
        Pair<String, String> avg = new Pair<>( "Avg" + capName, avgTicks + ending.apply( avgTicks ) );

        long medianTicks = sorted.size() % 2 == 0
                ? (sorted.get( sorted.size() / 2 ) + sorted.get( sorted.size() / 2 + 1 )) / 2
                : sorted.get( sorted.size() / 2 + 1 );
        Pair<String, String> median = new Pair<>( "Median" + capName, medianTicks + ending.apply( medianTicks ) );

        long maxTicks = sorted.stream().max( Long::compareTo ).orElseThrow();
        Pair<String, String> max = new Pair<>( "Max" + capName, maxTicks + ending.apply( maxTicks ) );

        long minTicks = sorted.stream().min( Long::compareTo ).orElseThrow();
        Pair<String, String> min = new Pair<>( "Min" + capName, minTicks + ending.apply( minTicks ) );

        return List.of( avg, median, max, min );
    }


    private void executeAnalysis( Consumer<ObjectNode> consumer ) {
        List<ResultWorker> workers = new ArrayList<>();

        JsonSource source = JsonSource.of( context.getConfig().getResultFile( this.workload.getName() ), 10_000 );

        long lines = source.countLines();
        long chunk = lines / WORKERS;

        for ( int i = 0; i < WORKERS; i++ ) {
            JsonSource workerSource = source.copy();
            workerSource.offset( chunk * i );
            workers.add( new ResultWorker( consumer, workerSource, chunk, context, this ) );
        }

        workers.forEach( ResultWorker::start );

        try {
            for ( ResultWorker w : workers ) {
                w.join();
            }
        } catch ( InterruptedException e ) {
            throw new RuntimeException( e );
        }
    }


}
