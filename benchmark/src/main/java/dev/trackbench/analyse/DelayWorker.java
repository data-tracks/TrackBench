package dev.trackbench.analyse;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.trackbench.configuration.BenchmarkContext;
import dev.trackbench.util.ObservableThread;
import dev.trackbench.util.file.JsonIterator;
import java.io.File;
import java.util.Objects;

public class DelayWorker extends ObservableThread {

    private final File target;
    private final BenchmarkContext context;
    private final DelayCollector collector;


    public DelayWorker( File target, BenchmarkContext context, DelayCollector collector ) {
        this.target = target;
        this.context = context;
        this.collector = collector;
    }


    @Override
    public void run() {
        JsonIterator iterator = new JsonIterator( context.getConfig().readBatchSize(), target, false );

        while ( iterator.hasNext() ) {
            ObjectNode node = (ObjectNode) iterator.next();

            long receivedTick = Objects.requireNonNull( node ).get( "tick" ).asLong();
            ObjectNode value = (ObjectNode) node.get( "data" );
            long sendTick = value.get( "tick" ).asLong();

            collector.delays.add( receivedTick - sendTick );

        }
    }

}
