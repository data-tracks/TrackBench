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

    private final long maxId;
    private final JsonSource truth;

    private JsonSource test;

    private Function<JsonNode, Long> extractor;

    private List<Long> missing = new ArrayList<>();


    public Comparator(
            JsonSource truth,
            JsonSource test,
            Function<JsonNode, Long> extractor) {
        this.truth = truth;
        this.test = test;
        this.extractor = extractor;
        this.maxId = MaxCounter.extractMax(truth, t -> t.get("id").asLong());
    }


    public void compare() {

        CountRegistry registry = new CountRegistry( maxId, 100_00, " id" );
        long testId = extractor.apply(test.next());
        for (long i = 0; i < maxId; i++) {
            long id = extractor.apply(truth.next());
            if( id != i){
                throw new RuntimeException("Truth should contain all ids");
            }

            if (id < testId) {
                missing.add(i);
            }else {
                testId = extractor.apply(test.next());
            }
            if( i % 100_000 == 0 ){
                registry.update(0, i);
            }
        }
        registry.done();
        log.info("Found {} missing entries", missing.size());

    }




}
