package dev.datageneration.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.datageneration.simulation.BenchmarkConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class JsonIterator {

    private final BenchmarkConfig benchmarkConfig;
    private final ObjectMapper mapper = new ObjectMapper();
    List<JsonNode> cache = new ArrayList<>();
    private final BufferedReader reader;
    @Getter
    private long counter = 0;


    public JsonIterator(BenchmarkConfig benchmarkConfig, File target) {
        this.benchmarkConfig = benchmarkConfig;
        try {
            countLines(target);
            this.reader = new BufferedReader(new FileReader(target));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void countLines(File target) throws FileNotFoundException {
        BufferedReader counter = new BufferedReader(new FileReader(target));
        AtomicLong count = new AtomicLong();
        counter.lines().forEach(line -> count.getAndIncrement());
        log.info("File {} has {} lines", target.getName(), count.get());
    }

    @Nullable
    public JsonNode next() {
        if (hasNext()) {
            counter++;
            return cache.removeFirst();
        }else {
            return null;
        }
    }

    public boolean hasNext() {
        if (!cache.isEmpty()) {
            return true;
        }

        try {
            for (long i = 0; i < benchmarkConfig.readBatchSize(); i++) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                cache.add(mapper.readTree(line));
            }
            return !cache.isEmpty();
        }catch (Exception e) {
            return false;
        }
    }
}
