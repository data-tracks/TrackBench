package dev.kafka.tools;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kafka.average.AverageFuelPumpGroup;
import dev.kafka.json.JsonFuelPump;
import dev.kafka.serialize.AverageFuelPumpGroupSerde;
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
public class FuelPumpGroup {

    static Properties props = new Properties();
    static Properties producerProps = new Properties();
    static Producer<String, String> producer;
    static long windowSize = 1000;
    static long advanceBy = 250;
    static String inputTopic = "fuelPump-group";
    static String outputTopic = "output";
    static ObjectMapper mapper = new ObjectMapper();

    public static void getProps(Properties props) {
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE); // Ensure exactly-once semantics
//        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 4);
    }

    public static void main(String[] args) {
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "fuelPump-group-app");
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

//            sensorStream.foreach((key, value) -> {
//                log.info("Key: " + key + " Value: " + value);
//            });

            KTable<Windowed<String>, AverageFuelPumpGroup> aggregatedStream = sensorStream
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
                    .aggregate(() -> new AverageFuelPumpGroup(0, 0, 0, 0, 0, -1), (key, value, agg) -> {
                        JsonFuelPump entry = new JsonFuelPump(value);
                        if (!entry.error) {
                            agg.count += 1;
                            agg.temp += entry.temp;
                            agg.flowRate += entry.flowRate;
                            if (agg.tickEnd < entry.tick) {
                                agg.tickEnd = entry.tick;
                            }
                            if (agg.id == -1) {
                                agg.id = entry.id;
                                agg.tickStart = entry.tick;
                            }
                            checkMinMax(agg, entry);
                        }
                        return agg;
                    }, Materialized.with(Serdes.String(), new AverageFuelPumpGroupSerde()));//.suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded()));

            aggregatedStream
                    .toStream()
//                    .filter((key, value) -> {
//                        long windowEnd = key.window().end();
//                        return value.getTickEnd() >= windowEnd;
//                    })
                    .foreach((key, value) -> {

                        double[] average = value.getAverage();

//                        String message = "AverageTire Temp: " + average[0] + " AverageTire Pressure: " + average[1] + " Count: " + value.getCount();
                        JSONObject data = new JSONObject();
                        data.put("averageTemp", average[0]);
                        data.put("averageFlowRate", average[1]);
                        data.put("id", value.getId());
                        data.put("type", "fuelPump");

                        JSONObject json = new JSONObject();
                        json.put("startTime", value.getTickStart());
                        json.put("endTime", value.getTickEnd());
                        json.put("data", data);

                        String jsonMessage = json.toString();

                        log.info("Message: " + jsonMessage);
                        producer.send(new ProducerRecord<>(outputTopic, "0", jsonMessage), (metadata, exception) -> {
                            if (exception != null) {
                                System.err.println("Failed to send message: " + exception.getMessage());
                                // Optionally, handle the failure (e.g., retry logic)
                            }
//                            else {
//                                log.info("Message sent to topic " + outputTopic + ": AverageTire Temp" + average[0] + " AverageTire Pressure" + average[1]);
//                            }
                        });
                    });

            // Start the Kafka Streams application
            KafkaStreams streams = new KafkaStreams(builder.build(), props);
            streams.cleanUp();
            streams.start();

            // Graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
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

    private static void checkMinMax(AverageFuelPumpGroup agg, JsonFuelPump entry) {
        if (agg.maxTemp < entry.temp) {
            agg.maxTemp = entry.temp;
        }
        if (agg.minTemp > entry.temp) {
            agg.minTemp = entry.temp;
        }

        if (agg.maxFlow < entry.flowRate) {
            agg.maxFlow = entry.flowRate;
        }
        if (agg.minFlow > entry.flowRate) {
            agg.minFlow = entry.flowRate;
        }
    }
}
