package dev.trackbench.simulation.type;

import com.fasterxml.jackson.databind.node.TextNode;
import dev.trackbench.simulation.RandomData;

import java.util.List;

public record StringType() implements DataType {

    @Override
    public TextNode sample(String name) {
        String[] dir = new String[] {"left", "right", "straight"};
        int num = (int)(RandomData.getRandom(0,2));
        return TextNode.valueOf( dir[num] );
    }

    @Override
    public List<Object> getData() {
        return List.of();
    }
}
