package dev.datageneration.aggregate;

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
        }catch ( Exception e ) {
            throw new RuntimeException( e );
        }

    }


    @Override
    public List<JsonNode> reset() {
        double result = number / count;
        DoubleNode value = DoubleNode.valueOf(result);

        count = 0;
        number = 0;

        return List.of(value);
    }

}
