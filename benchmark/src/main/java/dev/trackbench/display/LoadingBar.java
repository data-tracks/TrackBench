package dev.trackbench.display;

public class LoadingBar implements Component{


    private final String unit;
    private final long maxPercent;
    // Total number of steps for the loading bar
    int totalSteps = 50;
    double total;


    public LoadingBar(long total, String unit ) {
        this.total = total;
        this.unit = unit;
        this.maxPercent = 100;
    }


    public void next(long count) {
        int percentage = (int) (count / total * maxPercent);

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
        bar.append( "] " )
                .append( String.format( "%,d", count).replace( ",", "'" ) )
                .append( " of " )
                .append( String.format( "%,d", (long) total).replace( ",", "'" ) )
                .append( unit );

        // Print the loading bar
        System.out.print( "\r" + bar ); // '\r' returns the cursor to the beginning of the line

    }

    public void done() {
        this.next((long) total);
        System.out.print( "\n" );
    }

    @Override
    public void render() {
        next( 0 );
    }
}
