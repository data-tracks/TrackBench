package dev.trackbench.sending;

import com.fasterxml.jackson.databind.JsonNode;
import dev.trackbench.simulation.processing.Value;
import dev.trackbench.util.JsonIterator;
import dev.trackbench.util.ObservableThread;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Filler extends ObservableThread {

    public static final String TICK = "tick";
    private final BlockingQueue<Value> queue;
    private final JsonIterator iterator;
    private final Thread afterRunning;


    public Filler( BlockingQueue<Value> queue, JsonIterator iterator, Thread afterRunning ) {
        this.queue = queue;
        this.iterator = iterator;
        this.afterRunning = afterRunning;
    }


    @Override
    public void run() {
        try {
            ready.set( true );
            while ( iterator.hasNext() && running.get() ) {
                JsonNode node = iterator.next();
                long tick = node.get( TICK ).asLong();
                queue.put( new Value( tick, node ) );
            }
            log.info( "Filler for file {} finished", iterator.getFile().getName() );

            while ( !queue.isEmpty() ){
                Thread.sleep( 100 );
            }
            this.afterRunning.interrupt();
        }catch( InterruptedException e ) {
            throw new RuntimeException( e );
        }

    }

}
