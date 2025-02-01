package dev.trackbench.execution.sending;

import com.fasterxml.jackson.databind.JsonNode;
import dev.trackbench.configuration.BenchmarkConfig;
import dev.trackbench.simulation.processing.Value;
import dev.trackbench.util.Clock;
import dev.trackbench.util.ObservableThread;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

public class Sender extends ObservableThread {

    private final long id;
    BlockingQueue<Value> messageQueue = new ArrayBlockingQueue<>(20_000);

    final Consumer<JsonNode> messageConsumer;

    final Clock clock;

    public final AtomicBoolean running = new AtomicBoolean(true);

    final BenchmarkConfig config;

    final long waitTimeNs;

    public final AtomicLong count = new AtomicLong();


    public Sender( long id, BenchmarkConfig config, Consumer<JsonNode> messageConsumer, Clock clock) {
        this.id = id;
        this.messageConsumer = messageConsumer;
        this.clock = clock;
        this.config = config;
        this.waitTimeNs = config.stepDurationNs();
    }


    @Override
    public void run() {
        try {
            ready.set(true);
            while ( running.get() ) {
                Value element = messageQueue.take();
                while ( clock.tick() < element.getTick() ){
                    LockSupport.parkNanos(1000);
                }
                messageConsumer.accept( element.getNode() );
                if(count.incrementAndGet() % 10_000 == 0){
                    //registry.update( id, count.get() );
                }

            }
        }catch( InterruptedException e ) {
            // this is intended
        }

    }

}
