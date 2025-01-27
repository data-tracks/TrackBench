package dev.datageneration.simulation.types;

import com.fasterxml.jackson.databind.node.TextNode;
import dev.datageneration.simulation.RandomData;

import dev.datageneration.simulation.types.DataType.NumericType;
import java.util.List;

public record IntType(int min, int max) implements DataType, NumericType {

    @Override
    public TextNode sample(String name) {
        return TextNode.valueOf(String.valueOf((int)(Math.round(RandomData.getRandomWithProbability(min, max, name)))));
    }

    @Override
    public List<Object> getData() {
        return List.of();
    }
}
