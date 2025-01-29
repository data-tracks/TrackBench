package dev.trackbench.simulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Value;

@Value(staticConstructor = "of")
public class ErrorRates {

    double noDataRate;
    double dropRate;



    public JsonNode toJson() {
        ObjectNode obj = JsonNodeFactory.instance.objectNode();
        obj.put("noDataRate", noDataRate);
        obj.put("dropRate", dropRate);
        return obj;
    }

    public static ErrorRates fromJson( ObjectNode errorRates ) {
        return new ErrorRates(errorRates.get("noDataRate").asDouble(), errorRates.get("dropRate").asDouble());
    }

}
