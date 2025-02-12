package dev.trackbench.system;

import com.fasterxml.jackson.databind.JsonNode;
import dev.trackbench.configuration.workloads.Workload;
import dev.trackbench.execution.receiver.Buffer;
import dev.trackbench.util.Clock;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class DummySystem implements System {

    public static final int CAPACITY = 2_000_000;
    private final Map<String, BlockingQueue<String>> queues = new ConcurrentHashMap<>();


    @Override
    public void prepare() {

    }


    @Override
    public Consumer<JsonNode> getSender() {
        Collection<BlockingQueue<String>> queues = this.queues.values();
        return json -> queues.forEach( q -> q.add( json.toString() ) );
    }


    @Override
    public Runnable getReceiver( Workload workload, AtomicBoolean running, AtomicBoolean ready, Clock clock, Buffer dataConsumer ) {
        queues.putIfAbsent( workload.getName(), new ArrayBlockingQueue<>( CAPACITY ) );
        return () -> {
            try {
                ready.set( true );
                while ( running.get() ) {
                    String value = queues.get( workload.getName() ).take();

                    dataConsumer.attach( clock.tick(), value );
                }
            } catch ( Exception e ) {
                // all good
            }
        };
    }

}
