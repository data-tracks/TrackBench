package dev.trackbench.validation;

import dev.trackbench.display.DisplayUtils;
import dev.trackbench.util.file.FileUtils;
import dev.trackbench.util.file.JsonSource;
import java.io.File;
import java.util.Objects;

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

        long diff = Math.abs( right - left );
        long percent =  Math.abs( right - left ) * 100 / Math.max( left, right );
        System.out.printf( "%s data points in %s and %s data points in %s, difference %s data points | %d%%\n",
                DisplayUtils.printNumber(left), this.left, DisplayUtils.printNumber(right), this.right, DisplayUtils.printNumber(diff), percent );
    }

}
