package dev.trackbench.validation;

import com.fasterxml.jackson.databind.JsonNode;
import dev.trackbench.util.file.JsonSource;
import lombok.Getter;

public class MaxCounter extends Thread {


    private final long start;
    private final long end;
    private final JsonSource target;
    @Getter
    private long maxTick = -1;


    public MaxCounter( long start, long end, JsonSource target ) {
        this.start = start;
        this.end = end;
        this.target = target;
        target.offset( start );
    }


    @Override
    public void run() {
        for ( long i = 0; i < end - start; i++ ) {
            if ( !target.hasNext() ) {
                return;
            }
            JsonNode next = target.next();
            this.maxTick = Math.max( this.maxTick, next.get( "tick" ).asLong() );
        }

    }

}
