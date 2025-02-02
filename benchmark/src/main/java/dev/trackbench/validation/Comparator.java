package dev.trackbench.validation;

import com.fasterxml.jackson.databind.JsonNode;
import dev.trackbench.util.CountRegistry;
import dev.trackbench.util.file.JsonSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import dev.trackbench.validation.max.MaxCounter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Comparator {

    public final static long WORKERS = 32;

    private final long maxTick;
    private final JsonSource left;
    private final Function<JsonNode, Long> leftTickExtractor;

    private JsonSource right;
    private final Function<JsonNode, Long> rightTickExtractor;
    
    private final BiFunction<JsonNode, JsonNode, String> comparator;

    private Map<Long, String> issues;


    public Comparator(
            JsonSource left,
            Function<JsonNode, Long> leftTickExtractor,
            JsonSource right,
            Function<JsonNode, Long> rightTickExtractor,
            BiFunction<JsonNode, JsonNode, String> comparator ) {
        this.left = left;
        this.leftTickExtractor = leftTickExtractor;
        this.right = right;
        this.rightTickExtractor = rightTickExtractor;
        this.comparator = comparator;
        this.maxTick = MaxCounter.extractMax(left, t -> t.get("tick").asLong());
        log.info( "Max tick: {}", maxTick );
    }


    public void compare() {
        long chunks = maxTick / WORKERS;

        CountRegistry registry = new CountRegistry( maxTick, 10, " ticks" );

        List<CompareWorker> workers = new ArrayList<>();
        for ( long i = 0; i < WORKERS; i++ ) {
            CompareWorker worker = new CompareWorker( i, i + chunks, left.copy(), leftTickExtractor, right.copy(), rightTickExtractor, comparator, registry );
            worker.start();
            workers.add( worker );
        }

        try {
            for ( CompareWorker worker : workers ) {
                worker.join();
                this.issues.putAll( worker.issues );
            }
        } catch ( InterruptedException e ) {
            throw new RuntimeException( e );
        }
    }


    public static class CompareWorker extends Thread {

        private final static long UPDATE_INTERVAL = 1;


        private final long start;
        private final long end;
        private final JsonSource left;
        private final Function<JsonNode, Long> leftTickExtractor;
        private final JsonSource right;
        private final Function<JsonNode, Long> rightTickExtractor;
        private final BiFunction<JsonNode, JsonNode, String> comparator;
        private final Map<Long, String> issues = new HashMap<>();
        private final CountRegistry registry;


        public CompareWorker(
                long start,
                long end,
                JsonSource left,
                Function<JsonNode, Long> leftTickExtractor,
                JsonSource right,
                Function<JsonNode, Long> rightTickExtractor,
                BiFunction<JsonNode, JsonNode, String> comparator,
                CountRegistry registry ) {
            this.start = start;
            this.end = end;
            this.left = left;
            this.leftTickExtractor = leftTickExtractor;
            this.right = right;
            this.rightTickExtractor = rightTickExtractor;
            this.comparator = comparator;
            this.registry = registry;
        }

        @Override
        public void run() {

            for ( long i = 0; i < end - start; i++ ) {
                long currentTick = i + start;

                left.reset();
                List<JsonNode> lefts = new ArrayList<>();
                while ( left.hasNext() ){
                    JsonNode current = left.next();
                    long tick = leftTickExtractor.apply(current);

                    if ( tick == currentTick ) {
                        lefts.add( current);
                    }
                }
                List<JsonNode> rights = new ArrayList<>();
                while ( right.hasNext() ){
                    JsonNode current = right.next();
                    long tick = rightTickExtractor.apply(current);
                    if ( tick == currentTick ) {
                        rights.add( current);
                    }
                }

                if ( lefts.size() != rights.size() ) {
                    issues.put( currentTick, "difference is: " + issues.get( i ) );
                }

                if( i % UPDATE_INTERVAL == 0){
                    registry.update( start, i );
                }

            }

        }


    }


}
