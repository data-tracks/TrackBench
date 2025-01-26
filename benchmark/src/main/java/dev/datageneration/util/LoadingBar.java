package dev.datageneration.util;

public class LoadingBar {


    // Total number of steps for the loading bar
    int totalSteps = 50;
    double total;


    public LoadingBar(long total) {
        System.out.println( "Loading..." );
        this.total = total;
        next( 0 );
    }


    public void next(long count) {
        int percentage = (int) (100 / (total / (count + 1)));

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
                .append( "% | " )
                .append( String.format( "%,d", count).replace( ",", "'" ) )
                .append( " of " )
                .append( String.format( "%,d", (long) total).replace( ",", "'" ) );

        // Print the loading bar
        System.out.print( "\r" + bar ); // '\r' returns the cursor to the beginning of the line

    }

}
