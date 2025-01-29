package dev.trackbench.system;

import com.fasterxml.jackson.databind.JsonNode;
import dev.trackbench.receiver.Buffer;
import dev.trackbench.util.Clock;
import dev.trackbench.workloads.Workload;
import io.javalin.Javalin;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class DummySystem implements System {

    private BlockingQueue<String> queue = new ArrayBlockingQueue<>( 2_000_000 );

    Javalin app;

    Thread thread;


    @Override
    public void prepare() {

    }


    @Override
    public Consumer<JsonNode> getSender() {
        return json -> queue.add( json.toString() );

    }


    @Override
    public Runnable getReceiver( Workload workload, AtomicBoolean running, AtomicBoolean ready, Clock clock, Buffer dataConsumer ) {
        return () -> {
            try {
                ready.set( true );
                while ( running.get() ) {
                    String value = queue.take();

                    dataConsumer.attach( clock.tick(), value );
                }
            } catch ( Exception e ) {
                // all good
            }
        };
    }

}
