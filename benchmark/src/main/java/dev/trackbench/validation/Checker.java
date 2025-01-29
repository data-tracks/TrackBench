package dev.trackbench.validation;

import dev.trackbench.util.DisplayUtils;
import dev.trackbench.util.FileUtils;
import java.io.File;
import java.util.Objects;

public class Checker {

    final File left;
    final File right;


    public Checker( File left, File right ) {
        this.left = left;
        this.right = right;
    }


    public void check() {
        long left = countLines( this.left );
        long right = countLines( this.right );

        long diff = Math.abs( right - left );
        long percent =  Math.abs( right - left ) * 100 / Math.max( left, right );
        System.out.printf( "%s data points in %s and %s data points in %s, difference %s data points | %d%%\n",
                DisplayUtils.printNumber(left), this.left, DisplayUtils.printNumber(right), this.right, DisplayUtils.printNumber(diff), percent );
    }


    private long countLines( File file ) {
        long lines = 0;
        if ( file.isDirectory() ) {
            for ( File f : Objects.requireNonNull( file.listFiles() ) ) {
                lines += countLines( f );
            }
        } else {
            if ( file.getName().endsWith( ".json" ) ) {
                return FileUtils.countLines( file );
            }
        }
        return lines;
    }

}
