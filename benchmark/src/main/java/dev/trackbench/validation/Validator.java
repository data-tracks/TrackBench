package dev.trackbench.validation;

import dev.trackbench.BenchmarkContext;

public class Validator {
    public static void start(BenchmarkContext context) {
        Sorter sorter = new Sorter(context);
        sorter.sort();

        Checker checker = new Checker();
        checker.check();
    }
}
