package dev.datageneration.util;

import dev.datageneration.Main;

public class LoadingBar {


    private final String units;
    private final long maxPercent;
    // Total number of steps for the loading bar
    int totalSteps = 50;
    double total;


    public LoadingBar(long total, String units) {
        System.out.println( "Loading..." );
        this.total = total;
        this.units = units;
        this.maxPercent = Math.min( total, 100 );
        next( 0 );
    }


    public void next(long count) {
        int percentage = (count == 0 ? 0 : (int) (maxPercent / (total / count)));

        // Build the loading bar
        StringBuilder bar = new StringBuilder();
        bar.append( "[" );
        for ( int j = 0; j < totalSteps; j++ ) {
            if ( j < (percentage/2) ) {
                bar.append( "=" );
            } else {
                bar.append( " " );
            }
        }
        bar.append( "] " )
                .append( percentage )
                .append( units )
                .append( " | " )
                .append( String.format( "%,d", count).replace( ",", "'" ) )
                .append( " of " )
                .append( String.format( "%,d", (long) total).replace( ",", "'" ) );

        // Print the loading bar
        System.out.print( "\r" + bar ); // '\r' returns the cursor to the beginning of the line

    }

}
