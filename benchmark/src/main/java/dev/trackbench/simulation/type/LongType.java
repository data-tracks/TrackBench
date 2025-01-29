package dev.trackbench.simulation.type;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import dev.trackbench.simulation.RandomData;
import dev.trackbench.simulation.type.DataType.NumericType;
import java.util.List;

public record LongType(int min, int max) implements DataType, NumericType {

    @Override
    public JsonNode sample( String name ) {
        return DoubleNode.valueOf( RandomData.getRandomWithProbability( min, max, name ) );
    }


    @Override
    public List<Object> getData() {
        return List.of();
    }

}
