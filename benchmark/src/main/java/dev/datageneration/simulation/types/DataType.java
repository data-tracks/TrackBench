package dev.datageneration.simulation.types;

import java.util.List;

public interface DataType {
    Object sample(String name);

    List<Object> getData();
}
