package dev.datageneration.aggregate;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public interface Aggregator {

    void next( JsonNode object );

    List<JsonNode> get();

}
