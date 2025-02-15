package dev.trackbench.validation.max;

import com.fasterxml.jackson.databind.JsonNode;
import dev.trackbench.util.file.JsonSource;
import dev.trackbench.validation.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import lombok.Getter;

public class MaxCounter extends Thread {


    private final long start;
    private final long end;
    private final JsonSource target;
    private final Function<JsonNode, Long> tickExtractor;
    @Getter
    private long maxTick = -1;


    public MaxCounter( long start, long end, JsonSource target, Function<JsonNode, Long> numExtractor ) {
        this.start = start;
        this.end = end;
        this.target = target;
        target.offset( start );
        this.tickExtractor = numExtractor;

    }


    public static long extractMax( JsonSource source, Function<JsonNode, Long> numberExtractor ) {
        long lines = source.countLines();

        if ( lines < 1 ) {
            return -1;
        }

        long workers = Math.min( lines, Comparator.WORKERS );

        long chunks = lines / workers;

        List<MaxCounter> threads = new ArrayList<>();

        for ( long i = 0; i < lines; i += chunks ) {
            MaxCounter maxCounter = new MaxCounter( i, i + chunks, source.copy(), numberExtractor );
            maxCounter.start();
            threads.add( maxCounter );
        }

        try {
            for ( Thread thread : threads ) {
                thread.join();
            }
        } catch ( InterruptedException e ) {
            throw new RuntimeException( e );
        }

        long max = 0;
        for ( MaxCounter thread : threads ) {
            max = Math.max( max, thread.getMaxTick() );
        }
        return max;

    }


    @Override
    public void run() {
        for ( long i = 0; i < end - start; i++ ) {
            if ( !target.hasNext() ) {
                return;
            }
            JsonNode next = target.next();
            this.maxTick = Math.max( this.maxTick, tickExtractor.apply( next ) );
        }
    }

}
