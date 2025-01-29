package dev.trackbench.receiver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.trackbench.util.ObservableThread;
import dev.trackbench.util.Pair;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import lombok.Getter;

public class Buffer extends ObservableThread {

    final BlockingQueue<Pair<Long, String>> buffer = new ArrayBlockingQueue<>( 2_000_000 );

    final BiConsumer<Long, JsonNode> consumer;

    final ObjectMapper mapper = new ObjectMapper();

    @Getter
    long count = 0;


    public Buffer( BiConsumer<Long, JsonNode> consumer ) {
        this.consumer = consumer;
    }


    public void attach( long tick, String value ) {
        this.buffer.add( new Pair<>( tick, value ) );
    }


    public void run() {
        try {
            while ( running.get() ) {
                Pair<Long, String> pair = buffer.take();
                this.consumer.accept(pair.left(), mapper.readTree( pair.right() ) );
                count++;
            }
        } catch ( InterruptedException | JsonProcessingException e ) {
            // this is intended
        }

    }

}
