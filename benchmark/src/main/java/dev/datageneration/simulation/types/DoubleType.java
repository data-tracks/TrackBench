package dev.datageneration.simulation.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import dev.datageneration.simulation.RandomData;

import dev.datageneration.simulation.types.DataType.NumericType;
import dev.datageneration.util.FixedDequeue;

import java.util.List;
import java.util.Objects;

public final class DoubleType implements DataType, NumericType {
    private final double min;
    private final double max;

    private final FixedDequeue<Double> lastElements = new FixedDequeue<>(3);

    public DoubleType(double min, double max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public DoubleNode sample(String name) {
        double value = Math.round(lastElements.isEmpty() ? RandomData.getRandomWithProbability(min, max, name ) : RandomData.getRandomWithProbability(min, max, name, lastElements.getLast() ));
        lastElements.add(value);
        return DoubleNode.valueOf(value);
    }

    @Override
    public List<Object> getData() {
        return List.of();
    }


}
