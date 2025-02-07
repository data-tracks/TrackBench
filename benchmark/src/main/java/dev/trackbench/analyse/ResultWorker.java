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
    private final long chunkSize;


    public ResultWorker(
            Consumer<ObjectNode> nodeConsumer,
            JsonSource source,
            long chunkSize,
            BenchmarkContext context,
            Analyser collector ) {
        this.chunkSize = chunkSize;
        this.source = source;
        this.context = context;
        this.collector = collector;
        this.nodeConsumer = nodeConsumer;
    }


    @Override
    public void run() {
        for (long i = 0; i < chunkSize; i++) {
            if (!source.hasNext()){
                break;
            }
            ObjectNode node = (ObjectNode) source.next();

            nodeConsumer.accept(node);
        }
    }

}
