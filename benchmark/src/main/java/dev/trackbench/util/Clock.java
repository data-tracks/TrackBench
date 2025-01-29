package dev.trackbench.util;

import dev.trackbench.BenchmarkConfig;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Clock extends Thread {

    private final AtomicLong tick = new AtomicLong(-1);
    private final BenchmarkConfig config;
    private final ScheduledExecutorService scheduler;


    public Clock( BenchmarkConfig config ) {
        this.config = config;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void run() {
        // the clock starts with some delay to get all workers started correctly
        scheduler.scheduleAtFixedRate( tick::incrementAndGet, 100, config.stepDurationNs(), TimeUnit.NANOSECONDS);
    }

    public long tick() {
        return tick.get();
    }
}
