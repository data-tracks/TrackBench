package dev.trackbench.display;

import dev.trackbench.util.Clock;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClockDisplay implements Component {

    final Clock clock;
    private final ScheduledExecutorService scheduler;

    long time = 0;
    long tick = 0;
    private Runnable finish;
    boolean notShowing = false;

    public ClockDisplay( Clock clock ) {
        this.clock = clock;
        this.scheduler = Executors.newScheduledThreadPool( 1 );
    }

    public void stop(){
        this.scheduler.shutdownNow();
        System.out.print("\r");
        if ( finish == null ) {
            notShowing = true;
            return;
        }
        this.finish.run();
    }


    public void update() {
        long nowTick = clock.tick();
        long nowTime = System.currentTimeMillis();

        double delay = nowTick == tick ? 0 : ((double) (nowTime - time) / (nowTick - tick));

        tick = nowTick;
        time = nowTime;

        synchronized ( this ) {
            System.out.print( "\r🕙 Clock: " + DisplayUtils.printNumber( tick ) + " per Tick " + delay + " ms" );
        }
    }

    @Override
    public void render() {
        // nothing on purpose
    }

    @Override
    public void start(Runnable runnable) {
        if ( notShowing ) {
            runnable.run();
            return;
        }
        scheduler.scheduleAtFixedRate( this::update, 100, 500, TimeUnit.MILLISECONDS);
        this.finish = runnable;
    }
}
