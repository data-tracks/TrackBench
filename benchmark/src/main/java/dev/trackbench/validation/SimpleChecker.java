package dev.trackbench.validation;

import dev.trackbench.display.Display;
import dev.trackbench.display.DisplayUtils;
import dev.trackbench.util.file.JsonSource;

public class SimpleChecker {

    final JsonSource left;
    final JsonSource right;


    public SimpleChecker( JsonSource left, JsonSource right ) {
        this.left = left;
        this.right = right;
    }


    public void check() {
        long left = this.left.countLines();
        long right = this.right.countLines();

        if ( left == 0 && right == 0 ) {
            throw new RuntimeException( "No data to check" );
        }

        long diff = Math.abs( right - left );
        long percent = Math.abs( right - left ) * 100 / Math.max( left, right );
        Display.INSTANCE.info( "%s data points in %s \n%s%s data points in %s \n%sDifference %s data points | %d%%\n",
                DisplayUtils.printNumber( left ), this.left, Display.indent(), DisplayUtils.printNumber( right ), this.right, Display.indent(), DisplayUtils.printNumber( diff ), percent );
    }

}
