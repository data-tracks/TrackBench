package dev.datageneration.simulation.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import dev.datageneration.simulation.RandomData;
import dev.datageneration.simulation.types.DataType.NumericType;
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
