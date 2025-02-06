package dev.trackbench.analyse;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.trackbench.configuration.BenchmarkContext;
import dev.trackbench.util.ObservableThread;
import dev.trackbench.util.file.JsonSource;

import java.io.File;
import java.util.function.Consumer;

public class ResultWorker extends ObservableThread {

    private final JsonSource source;
    private final BenchmarkContext context;
    private final Analyser collector;
    private final Consumer<ObjectNode> nodeConsumer;


    public ResultWorker(
            Consumer<ObjectNode> nodeConsumer,
            File source,
            BenchmarkContext context,
            Analyser collector ) {
        this.source = JsonSource.of(source, 10_000 );
        this.context = context;
        this.collector = collector;
        this.nodeConsumer = nodeConsumer;
    }


    @Override
    public void run() {
        while ( source.hasNext() ) {
            ObjectNode node = (ObjectNode) source.next();

            nodeConsumer.accept(node);
        }
    }

}
