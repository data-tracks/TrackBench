package dev.trackbench.display;

import java.util.concurrent.TimeUnit;

public class Timer extends Component {

    private final long minutes;


    public Timer( long minutes ) {
        this.minutes = minutes;
    }


    @Override
    public void render() {
        try {
            Display.INSTANCE.nextLine();
            for ( long remainingSeconds = minutes * 60; remainingSeconds >= 0; remainingSeconds-- ) {
                long displayMinutes = remainingSeconds / 60;
                long displaySeconds = remainingSeconds % 60;

                System.out.printf( "\r%sTime left: %02d:%02d", Display.indent(), displayMinutes, displaySeconds );

                TimeUnit.SECONDS.sleep( 1 );
            }
            System.out.print( "\r" );
        } catch ( InterruptedException e ) {
            throw new RuntimeException( e );
        }

    }

}
