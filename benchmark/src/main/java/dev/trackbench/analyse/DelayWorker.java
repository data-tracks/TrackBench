package dev.trackbench.analyse;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.trackbench.configuration.BenchmarkConfig;
import dev.trackbench.configuration.BenchmarkContext;
import dev.trackbench.util.ObservableThread;
import dev.trackbench.util.file.JsonSource;

import java.io.File;
import java.util.Objects;

public class DelayWorker extends ObservableThread {

    private final JsonSource source;
    private final BenchmarkContext context;
    private final LatencyAnalyser collector;


    public DelayWorker(File source, BenchmarkContext context, LatencyAnalyser collector ) {
        this.source = JsonSource.of(source, 10_000 );
        this.context = context;
        this.collector = collector;
    }


    @Override
    public void run() {

        while ( source.hasNext() ) {
            ObjectNode node = (ObjectNode) source.next();

            long receivedTick = BenchmarkConfig.getArrivedTick(Objects.requireNonNull( node ));
            long sendTick = node.get( "tick" ).asLong();

            collector.delays.add( receivedTick - sendTick );

        }
    }

}
