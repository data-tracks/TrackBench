package dev.trackbench.simulation.aggregate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import java.util.List;

public class AvgAggregator implements Aggregator {

    long count = 0;
    double number = 0;

    @Override
    public void next( JsonNode value ) {
        try {
            number += value.asDouble();
            count++;
        }catch ( Exception e ) {
            throw new RuntimeException( e );
        }

    }


    @Override
    public List<JsonNode> get() {
        double result = number / count;
        DoubleNode value = DoubleNode.valueOf(result);

        return List.of(value);
    }

}
