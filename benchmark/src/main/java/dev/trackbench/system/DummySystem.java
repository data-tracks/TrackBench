package dev.trackbench.system;

import com.fasterxml.jackson.databind.JsonNode;
import dev.trackbench.receiver.Buffer;
import dev.trackbench.util.Clock;
import io.javalin.Javalin;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.time.Duration;
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
    public Runnable getReceiver( AtomicBoolean running, AtomicBoolean ready, Clock clock, Buffer dataConsumer ) {
        return () -> {
            try {
                ready.set( true );
                while ( running.get() ) {
                    String value = queue.take();

                    dataConsumer.attach( clock.tick(), value );
                }
            } catch ( Exception e ) {
                throw new RuntimeException( e );
            }
        };
    }

}
