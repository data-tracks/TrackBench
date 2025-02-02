package dev.trackbench.system.kafka.KafkaTools;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.trackbench.system.kafka.AverageClass.AverageEngine;
import dev.trackbench.system.kafka.AverageClass.AverageEngineGroup;
import dev.trackbench.system.kafka.JsonClass.JsonEngine;
import dev.trackbench.system.kafka.Serde.AverageEngineGroupSerde;
import dev.trackbench.system.kafka.Serde.AverageEngineSerde;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.json.JSONObject;

import java.time.Duration;
import java.util.Properties;

@Slf4j
public class EngineGroup {

    static Properties props = new Properties();
    static Properties producerProps = new Properties();
    static Producer<String, String> producer;
    static long windowSize = 1000;
    static long advanceBy = 250;
    static String inputTopic = "engine-group";
    static String outputTopic = "output";
    static ObjectMapper mapper = new ObjectMapper();

    public static void getProps(Properties props) {
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG,0);
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE); // Ensure exactly-once semantics
    }

    public static void main(String[] args) {
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "engine-group-app");
        getProps(props);

        // Configure producer
        producerProps.put("bootstrap.servers", "localhost:9092");
        producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put("acks", "all");  // Ensure that all replicas acknowledge the message
        producerProps.put("retries", 3);   // Retry on failure
        producerProps.put("enable.idempotence", "true");


        // Initialize the producer once
        producer = new KafkaProducer<>(producerProps);

        try {
            StreamsBuilder builder = new StreamsBuilder();
            KStream<String, String> sensorStream = builder.stream(inputTopic);


            KTable<Windowed<String>, AverageEngineGroup> aggregatedStream = sensorStream
                    .mapValues(value -> {
                        try {
                            JsonNode node = mapper.readTree(value);
                            return node.get("type").toString();
                        } catch (Exception e) {
                            return "unknown";
                        }
                    })
                    .selectKey((key, value) -> value) // Use "type" as the key
                    .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                    .windowedBy(TimeWindows.of(Duration.ofMillis(windowSize)).grace(Duration.ofMillis(0)).advanceBy(Duration.ofMillis(advanceBy)))
                    .aggregate(() -> new AverageEngineGroup(0, 0, 0, 0, 0, 0, 0, 0, 0, -1), (key, value, agg) -> {
                        JsonEngine entry = new JsonEngine(value);
                        if(!entry.error) {
                            agg.count += 1;
                            agg.temp += entry.temp;
                            agg.rpm += entry.rpm;
                            agg.fuelFlow += entry.fuelFlow;
                            agg.oilPressure += entry.oilPressure;
                            agg.fuelPressure += entry.fuelPressure;
                            agg.exhaust += entry.exhaust;
                            if(agg.tickEnd < entry.tick) {
                                agg.tickEnd = entry.tick;
                            }
                            if(agg.id == -1) {
                                agg.id = entry.id;
                                agg.tickStart = entry.tick;
                            }
                            checkMinMax(agg, entry);
                        }
                        return agg;
                    }, Materialized.with(Serdes.String(), new AverageEngineGroupSerde()))
                    ;

            aggregatedStream
                    .toStream()
                    .foreach((key, value) -> {

                        double[] average = value.getAverage();

//                        String message = "AverageTire Temp: " + average[0] + " AverageTire Pressure: " + average[1] + " Count: " + value.getCount();
                        JSONObject data = new JSONObject();
                        data.put("averageTemp", average[0]);
                        data.put("averageRPM", average[1]);
                        data.put("averageFuelFlow", average[2]);
                        data.put("averageOilPressure", average[3]);
                        data.put("averageFuelPressure", average[4]);
                        data.put("averageExhaust", average[5]);
                        data.put("id", value.getId());
                        data.put("type", "engine");

                        JSONObject json = new JSONObject();
                        json.put("startTime", value.getTickStart());
                        json.put("endTime", value.getTickEnd());
                        json.put("data", data);

                        String jsonMessage = json.toString();

                        log.info("Message: " + jsonMessage);
                        producer.send(new ProducerRecord<>(outputTopic, "0", jsonMessage), (metadata, exception) -> {
                            if (exception != null) {
                                System.err.println("Failed to send message: " + exception.getMessage());
                            }
                        });
                    });

            // Start the Kafka Streams application
            KafkaStreams streams = new KafkaStreams(builder.build(), props);
            streams.cleanUp();
            streams.start();

            // Graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try{
                    producer.flush();
                    streams.close();
                } finally {
                    log.info("Shutting down");
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void checkMinMax(AverageEngineGroup agg, JsonEngine entry) {
        if(agg.maxRPM < entry.rpm) {
            agg.maxRPM = entry.rpm;
        }
        if(agg.minRPM > entry.rpm) {
            agg.minRPM = entry.rpm;
        }

        if(agg.maxFuelP < entry.fuelPressure) {
            agg.maxFuelP = entry.fuelPressure;
        }
        if(agg.minFuelP > entry.fuelPressure) {
            agg.minFuelP = entry.fuelPressure;
        }

        if(agg.maxOilP < entry.oilPressure) {
            agg.maxOilP = entry.oilPressure;
        }
        if(agg.minOilP > entry.oilPressure) {
            agg.minOilP = entry.oilPressure;
        }

        if(agg.maxExhaust < entry.exhaust) {
            agg.maxExhaust = entry.exhaust;
        }
        if(agg.minExhaust > entry.exhaust) {
            agg.minExhaust = entry.exhaust;
        }
    }
}
