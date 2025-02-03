package dev.trackbench.validation.chunksort;

import com.fasterxml.jackson.databind.JsonNode;
import dev.trackbench.util.CountRegistry;
import dev.trackbench.util.SimpleCountRegistry;
import dev.trackbench.util.file.FileJsonTarget;
import dev.trackbench.util.file.FileUtils;
import dev.trackbench.util.file.JsonSource;
import dev.trackbench.util.file.JsonTarget;
import dev.trackbench.validation.max.MaxCounter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Function;

@Slf4j
public class ChunkSorter {

    public final static long IDS_PER_CHUNK = 100_000;
    public final static long WORKERS = 64;
    public final static long BATCH_SIZE = 100_000;
    public static final String MERGED = "merged";
    public static final String SORTED = "sorted";
    public static final String FINAL = "final";

    public final JsonSource source;
    public final File target;
    private final long maxId;
    private final long workerSize;
    private final long chunks;
    private final long lines;
    private final Function<JsonNode, Long> extractor;

    public ChunkSorter(JsonSource source, File target, Function<JsonNode, Long> extractor) {
        this.source = source;
        this.target = target;
        if (!target.isDirectory()) {
            throw new IllegalArgumentException("Chunk target must be a directory");
        }
        this.extractor = extractor;
        this.maxId = MaxCounter.extractMax(source, value -> value.get("id").asLong());
        this.lines = source.countLines();
        this.chunks = maxId / IDS_PER_CHUNK;
        log.info("Chunks to create {} chunks", chunks);
        this.workerSize = lines / WORKERS != 0 ? lines / WORKERS + 1 : lines / WORKERS;

    }

    public void chunk() {
        CountRegistry registry = new CountRegistry(this.workerSize, 1_000, " lines");

        List<ChunkWorker> workers = new ArrayList<>();
        for (long i = 0; i < WORKERS; i++) {
            ChunkWorker worker = new ChunkWorker(
                    i,
                    registry,
                    i * this.workerSize,
                    this.workerSize,
                    chunks,
                    source,
                    FileUtils.createFolder(target, getWorkerFolder(i)),
                    v -> v.get("id").asLong());
            worker.start();
            workers.add(worker);
        }

        try {
            for (ChunkWorker worker : workers) {
                worker.join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        registry.done();

    }

    public File sort() {
        chunk();
        File merged = createMergedFolder();
        collect(merged);
        File sorted = createSortedFolder();
        sortChunks(merged, sorted);
        return intoOneFile(sorted);
    }

    private File intoOneFile(File sorted) {
        File target = createFinalFile();
        for (long i = 0; i < chunks; i++) {
            File current = FileUtils.getJson(sorted, String.valueOf(i * IDS_PER_CHUNK));
            FileUtils.copy(current, target);
        }
        log.info("Sorted and merged {}", chunks);
        return target;
    }

    private File createFinalFile() {
        return FileUtils.getJson(target, FINAL);
    }

    private void sortChunks(File source, File target) {
        BlockingQueue<Long> chunks = new ArrayBlockingQueue<>((int) this.chunks);
        for (long i = 0; i < this.chunks; i++) {
            boolean success = chunks.offer(i * IDS_PER_CHUNK);
        }

        List<SortWorker> workers = new ArrayList<>();
        CountRegistry registry = new CountRegistry(this.chunks, 1," chunks", false);
        for (long i = 0; i < WORKERS; i++) {
            SortWorker worker = new SortWorker(chunks, source, target, extractor, registry);
            worker.start();
            workers.add(worker);
        }
        try {
            for (SortWorker worker : workers) {
                worker.join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        registry.done();
    }

    private void collect(File target) {
        for (long chunk = 0; chunk < chunks; chunk++) {
            long chunkStart = chunk * IDS_PER_CHUNK;
            File chunkTarget = FileUtils.getJson(target, String.valueOf(chunkStart));
            boolean coveredByOne = false;
            for (long i = 0; i < WORKERS; i++) {
                File workerFolder = new File(this.target, getWorkerFolder(i));
                if (FileUtils.hasJsonFile(workerFolder, String.valueOf(chunkStart))) {
                    FileUtils.copy(FileUtils.getJson(workerFolder, String.valueOf(chunkStart)), chunkTarget);
                    coveredByOne = true;
                }
            }
            if (!coveredByOne) {
                try {
                    chunkTarget.createNewFile();
                    log.warn("Chunk {} was not covered", chunk);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @NotNull
    private static String getWorkerFolder(long i) {
        return "w" + i;
    }

    private File createMergedFolder() {
        return FileUtils.createFolder(target, MERGED);
    }

    private File createSortedFolder() {
        return FileUtils.createFolder(target, SORTED);
    }

    private static JsonTarget getJsonTarget(File target, long start) {
        return new FileJsonTarget(FileUtils.getJson(target, String.valueOf(start)), 10_000);
    }

    public static class ChunkWorker extends Thread {
        private final long id;
        private final long lines;
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
                long lines,
                long chunks,
                JsonSource source,
                File target,
                Function<JsonNode, Long> tickExtractor) {
            this.id = id;
            this.target = target;
            this.lines = lines;
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
            for (long i = 0; i < lines; i++) {
                if (!source.hasNext()) {
                    break;
                }
                JsonNode current = source.next();
                long id = tickExtractor.apply(current);
                long chunk = id / IDS_PER_CHUNK;
                JsonTarget target = targets.get(chunk * IDS_PER_CHUNK);
                if (target == null) {
                    target = getJsonTarget(this.target, chunk * IDS_PER_CHUNK);
                    targets.put(chunk * IDS_PER_CHUNK, target);
                }
                target.attach(current);

                if (i % 1_000 == 0) {
                    registry.update(this.id, i);
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

    }

    private static class SortWorker extends Thread {
        private final BlockingQueue<Long> chunks;
        private final File source;
        private final File target;
        private final Function<JsonNode, Long> extractor;
        private final CountRegistry registry;

        public SortWorker(
                BlockingQueue<Long> chunks,
                File source,
                File target,
                Function<JsonNode, Long> extractor,
                CountRegistry registry) {
            this.chunks = chunks;
            this.source = source;
            this.target = target;
            this.extractor = extractor;
            this.registry = registry;
        }

        @Override
        public void run() {
            Long chunk = chunks.poll();
            while (chunk != null) {
                JsonSource file = JsonSource.of(FileUtils.getJson(this.source, String.valueOf(chunk)), 10_000);
                JsonTarget target = getJsonTarget(this.target, chunk);

                TreeSet<JsonNode> queue = new TreeSet<>(Comparator.comparing(extractor));
                while (file.hasNext()) {
                    JsonNode next = file.next();
                    queue.add(next);
                }
                for (JsonNode jsonNode : queue) {
                    target.attach(jsonNode);
                }
                synchronized (chunks) {
                    this.registry.update(0, this.registry.getLast() + 1);
                    chunk = chunks.poll();
                }

            }
        }
    }
}
