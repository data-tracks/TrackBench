package dev.trackbench.simulation.type;

import com.fasterxml.jackson.databind.node.DoubleNode;
import dev.trackbench.simulation.RandomData;

import dev.trackbench.simulation.type.DataType.NumericType;
import dev.trackbench.util.FixedDequeue;

import java.util.List;

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
