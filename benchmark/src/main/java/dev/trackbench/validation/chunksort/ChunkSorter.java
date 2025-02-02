package dev.trackbench.validation.chunksort;

import com.fasterxml.jackson.databind.JsonNode;
import dev.trackbench.util.CountRegistry;
import dev.trackbench.util.file.FileJsonTarget;
import dev.trackbench.util.file.FileUtils;
import dev.trackbench.util.file.JsonSource;
import dev.trackbench.util.file.JsonTarget;
import dev.trackbench.validation.max.MaxCounter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public class ChunkSorter {

    public final static long ID_PER_CHUNK = 100_000;
    public final static long WORKERS = 64;
    public final static long BATCH_SIZE = 100_000;

    public final JsonSource source;
    public final File target;

    public ChunkSorter(JsonSource source, File target) {
        this.source = source;
        this.target = target;
        if (!target.isDirectory()) {
            throw new IllegalArgumentException("Chunk target must be a directory");
        }
    }

    public void chunk(){
        long maxId = MaxCounter.extractMax(source, value -> value.get("id").asLong());
        long chunks = maxId / ID_PER_CHUNK;
        log.info("Chunks to create {} chunks", chunks);

        long chunkSize = maxId % ID_PER_CHUNK;

        if (maxId % ID_PER_CHUNK != 0) {
            chunkSize++;
        }

        CountRegistry registry = new CountRegistry(chunkSize, 10_000, " ids");

        List<ChunkWorker> workers = new ArrayList<>();
        for (long i = 0; i < WORKERS; i++) {
            ChunkWorker worker = new ChunkWorker(
                    i,
                    registry,
                    i*chunkSize,
                    chunkSize,
                    chunks,
                    source,
                    FileUtils.createFolder(target, "w"+i),
                    v -> v.get("id").asLong());
            worker.start();
            workers.add(worker);
        }

        try {
            for (ChunkWorker worker : workers) {
                worker.join();
            }
        }catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }


    public static class ChunkWorker extends Thread {
        private final long id;
        private final long chunkSize;
        private final File target;
        private final Map<Long, JsonTarget> targets = new HashMap<>();
        private final JsonSource source;
        private final Function<JsonNode, Long> tickExtractor;
        private final long chunks;
        private final CountRegistry registry;


        public ChunkWorker(
                long id,
                CountRegistry registry,
               long start,
               long chunkSize,
               long chunks,
               JsonSource source,
               File target,
               Function<JsonNode, Long> tickExtractor) {
            this.id = id;
            this.target = target;
            this.chunkSize = chunkSize;
            this.source = source.copy();
            this.source.offset(start);
            this.tickExtractor = tickExtractor;
            this.chunks = chunks;
            this.registry = registry;
        }

        @Override
        public void run() {
            separate();

        }

        private void separate() {
            for (long i = 0; i < chunkSize; i++) {
                if (!source.hasNext()){
                    break;
                }
                JsonNode current = source.next();
                long id = tickExtractor.apply(current);
                long chunk = id/chunkSize;
                JsonTarget target = targets.get(chunk*chunkSize);
                if (target == null) {
                    target = getJsonTarget(chunk*chunkSize);
                    targets.put(chunk*chunkSize, target);
                }
                target.attach(current);

                if (i%1_000 == 0) {
                    registry.update(id, chunk);
                }
            }

            try {
                for (JsonTarget f : this.targets.values()) {
                    f.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private JsonTarget getJsonTarget(long start) {
            return new FileJsonTarget(FileUtils.getJson(target, String.valueOf(start)), 10_000);
        }

    }


}
