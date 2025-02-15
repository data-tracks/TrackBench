package dev.trackbench.validation;

import com.fasterxml.jackson.databind.JsonNode;
import dev.trackbench.util.CountRegistry;
import dev.trackbench.util.file.JsonSource;
import dev.trackbench.validation.max.MaxCounter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Comparator {

    public final static long WORKERS = 32;

    private final long maxId;
    private final JsonSource truth;

    private final JsonSource test;

    private final Function<JsonNode, Long> extractor;

    private final List<Long> missing = new ArrayList<>();
    private final List<Long> nulls = new ArrayList<>();


    public Comparator(
            JsonSource truth,
            JsonSource test,
            Function<JsonNode, Long> extractor ) {
        this.truth = truth;
        this.test = test;
        this.extractor = extractor;
        this.maxId = MaxCounter.extractMax( truth, t -> t.get( "id" ).asLong() );
    }


    public List<String> compare() {
        CountRegistry registry = new CountRegistry( maxId, 10_000, " id", "compared" );
        long testId = extractor.apply( test.next() );
        long i = 0;
        while ( truth.hasNext() ) {
            long id = extractor.apply( truth.next() );

            if ( id < testId ) {
                missing.add( i );
            } else {
                JsonNode testValue = test.next();
                if ( testValue == null ) {
                    nulls.add( i );
                } else {
                    testId = extractor.apply( testValue );
                }

            }
            if ( i % 100_000 == 0 ) {
                registry.update( 0, i );
            }
            i++;
        }
        registry.done();

        return List.of(
                String.format( "Found %s missing entries %s", missing.size(), missing.subList( 0, Math.min( 20, missing.size() ) ) ),
                String.format( "Found %d null entries %s", nulls.size(), nulls ) );

    }


}
