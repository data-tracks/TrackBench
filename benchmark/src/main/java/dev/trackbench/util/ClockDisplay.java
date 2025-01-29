package dev.trackbench.util;

import dev.trackbench.BenchmarkConfig;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClockDisplay {

    private final BenchmarkConfig config;

    final Clock clock;
    private final ScheduledExecutorService scheduler;

    long time = 0;
    long tick = 0;

    public ClockDisplay( Clock clock, BenchmarkConfig config ) {
        this.clock = clock;
        this.config = config;
        System.out.println( "🕙 Clock: " + clock.tick() );
        this.scheduler = Executors.newScheduledThreadPool( 1 );
        scheduler.scheduleAtFixedRate( this::update, 100, 500, TimeUnit.MILLISECONDS);
    }

    public void stop(){
        this.scheduler.shutdownNow();
    }


    public void update() {
        long nowTick = clock.tick();
        long nowTime = System.currentTimeMillis();

        double delay = nowTick == tick ? 0 : ((double) (nowTime - time) / (nowTick - tick));

        tick = nowTick;
        time = nowTime;

        synchronized ( this ) {
            System.out.print( "\r🕙 Clock: " + tick + " per Tick " + delay + " ms" );
        }
    }

}
