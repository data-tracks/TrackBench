package dev.datageneration.simulation.types;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public interface DataType {
    JsonNode sample(String name);

    List<Object> getData();


    interface NumericType extends DataType {}
}
