package dev.datageneration.simulation.types;

import com.fasterxml.jackson.databind.node.LongNode;
import dev.datageneration.simulation.RandomData;

import dev.datageneration.simulation.types.DataType.NumericType;
import dev.datageneration.util.FixedDequeue;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

public final class NumberType implements DataType, NumericType {
    @Getter
    private final long min;

    @Getter
    private final long max;

    private final FixedDequeue<Long> lastElements = new FixedDequeue<>(3);

    public NumberType(long min, long max) {
        this.min = min;
        this.max = max;
    }


    @Override
    public LongNode sample(String name) {
        long value = Math.round(lastElements.isEmpty() ? RandomData.getRandomWithProbability(min, max, name ) : RandomData.getRandomWithProbability(min, max, name, lastElements.getLast() ));
        lastElements.add(value);
        return LongNode.valueOf(value);
    }

    @Override
    public List<Object> getData() {
        return List.of();
    }

}
