package dev.trackbench.util;

public class DisplayUtils {

    public static String printNumber(long number) {
        return String.format("%,d", number).replace(',', '\'');
    }
}
