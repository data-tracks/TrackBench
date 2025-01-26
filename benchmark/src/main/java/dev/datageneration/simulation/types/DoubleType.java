package dev.datageneration.simulation.types;

import dev.datageneration.simulation.RandomData;

import java.util.List;

public record DoubleType(double min, double max) implements DataType {

    @Override
    public Object sample(String name) {
        return RandomData.getRandomWithProbability(min, max, name);
    }

    @Override
    public List<Object> getData() {
        return List.of();
    }
}
