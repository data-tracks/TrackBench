package dev.trackbench.display;

import static dev.trackbench.display.Display.green;

import dev.trackbench.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;

public class LoadingBar implements Component{


    private final String unit;
    private final long maxPercent;
    // Total number of steps for the loading bar
    int totalSteps = 50;
    double total;
    List<Long> times = new ArrayList<>();
    long lastPercent = 0;


    public LoadingBar(long total, String unit ) {
        this.total = total;
        this.unit = unit;
        this.maxPercent = 100;
    }


    public void next(long count) {


        int percentage = (int) (count / total * maxPercent);
        if( lastPercent != percentage ) {
            this.lastPercent = percentage;
            this.times.add(System.currentTimeMillis());
        }

        // Build the loading bar
        StringBuilder bar = new StringBuilder();
        bar.append( "[" );
        for ( int j = 0; j < totalSteps; j++ ) {
            if ( j < (percentage/2) ) {
                bar.append( "█" );
            } else {
                bar.append( " " );
            }
            if( j == totalSteps/2){
                String percent = String.valueOf(percentage);
                int diff = 3 - percent.length();
                bar.append(" ").repeat(" ", diff).append( percentage ).append( "% " );
            }

        }
        bar.append( "] " );
        bar.append( String.format( "%,d", count).replace( ",", "'" ) )
                .append( " of " )
                .append( getTotal() );

        if(times.size() > 1) {
            double avgPercent = IntStream.range(1, times.size())
                    .mapToObj(i -> times.get(i) - times.get(i - 1))
                    .mapToLong(Long::longValue)
                    .average()
                    .orElseThrow();
            long duration = (long) ((100 - percentage) * avgPercent);
            bar.append(" | approx. ").append(TimeUtils.formatMillis(duration)).append(" to 100%");
        }

        // Print the loading bar
        System.out.print( "\r" + bar );

    }


    private @NotNull String getTotal() {
        return String.format( "%,d", (long) total ).replace( ",", "'" ) + unit;
    }


    public void done() {
        this.next((long) total);
        System.out.println( "\r" + green(getTotal()) );
    }

    @Override
    public void render() {
        next( 0 );
    }
}
