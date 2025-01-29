package dev.trackbench.util;

import java.time.Duration;

public class TimeUtils {

    public static String formatNanoseconds( long nanoseconds ) {
        Duration duration = Duration.ofNanos( nanoseconds );

        if ( duration.toDays() > 0 ) {
            return duration.toDays() + " days";
        } else if ( duration.toHours() > 0 ) {
            return duration.toHours() + " hours";
        } else if ( duration.toMinutes() > 0 ) {
            return duration.toMinutes() + " minutes";
        } else if ( duration.getSeconds() > 0 ) {
            return duration.getSeconds() + " seconds";
        } else if ( duration.toMillis() > 0 ) {
            return duration.toMillis() + " milliseconds";
        } else if ( duration.toNanos() >= 1000 ) {
            return duration.toNanos() / 1000 + " microseconds";
        } else {
            return nanoseconds + " nanoseconds";
        }
    }

}
