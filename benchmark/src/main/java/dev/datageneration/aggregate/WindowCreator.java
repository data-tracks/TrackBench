package dev.datageneration.aggregate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.datageneration.simulation.BenchmarkConfig;
import dev.datageneration.simulation.sensors.Sensor;
import dev.datageneration.simulation.sensors.SensorTemplate;
import dev.datageneration.simulation.types.DataType;
import dev.datageneration.simulation.types.IntType;
import dev.datageneration.util.JsonIterator;
import dev.datageneration.util.Pair;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class WindowCreator extends Thread{

    int windowSizeTicks = 100;

    List<Pair<Long, JsonNode>> cache = new ArrayList<>();

    private final BenchmarkConfig config;

    private final File target;
    private final ObjectMapper mapper = new ObjectMapper();

    private final SensorTemplate sensorTemplate;


    public WindowCreator(BenchmarkConfig config, File target) {
        this.config = config;
        this.target = target;
        List<String> names = Arrays.stream(target.getName().split("_")).toList();
        String id = names.getFirst();
        this.sensorTemplate = SensorTemplate.templates.get(Integer.parseInt(id));
    }

    @Override
    public void run() {
        JsonIterator iterator = new JsonIterator(config, target);

        DataType type = Sensor.dataTypes.get(sensorTemplate.getHeaders().getFirst());

        while (iterator.hasNext()){
            JsonNode node = iterator.next();
            JsonNode tickNode = node.get("tick");
            if (tickNode == null) {
                log.warn("Empty tick {}", tickNode );
                continue;
            }
            long tick = tickNode.asLong();
            while (cache.getFirst().getLeft() < (tick - windowSizeTicks)){
                cache.removeFirst();
            }
            if( type instanceof IntType intType){
                cache.add(new Pair<>(tick, node));
            }
        }
    }
}
