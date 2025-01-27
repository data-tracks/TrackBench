package dev.datageneration.aggregate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.LongNode;
import java.util.List;

public class CountAggregator implements Aggregator {

    long count = 0;

    @Override
    public void next( JsonNode object ) {
        count++;
    }


    @Override
    public List<JsonNode> reset() {
        LongNode result = LongNode.valueOf( count );
        count = 0;
        return List.of(result);
    }

}
