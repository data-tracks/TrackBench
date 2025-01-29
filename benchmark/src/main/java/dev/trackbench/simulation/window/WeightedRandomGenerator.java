package dev.trackbench.simulation.window;

import java.util.Random;

public class WeightedRandomGenerator {

    public static double generateRandomValue(Random random, double min, double max, double current) {
        double standardDeviation = (max - min) / 6; // 99.7% of values within 3 standard deviations
        double gaussian = random.nextGaussian();

        double value = current + gaussian * standardDeviation;

        // Clamp the value to the min and max range
        return Math.min(max, Math.max(min, value));
    }
}
