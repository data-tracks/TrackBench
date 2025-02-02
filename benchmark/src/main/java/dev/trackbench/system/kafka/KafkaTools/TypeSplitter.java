package dev.trackbench.system.kafka.KafkaTools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

import java.util.*;

@Slf4j
public class TypeSplitter {

    private static final Set<String> existingTopics = new HashSet<>();
    static Properties props = new Properties();
    static Properties producerProps = new Properties();
    static Producer<String, String> producer;
    static String inputTopic = "input";
    static String outputTopic = "output";
    static String errorTopic = "error";
    static ObjectMapper mapper = new ObjectMapper();

    //TODO: use this HashMap
    static Map<String, List<String>> keywords = new HashMap<>();


    public static void getProps(Properties props) {
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE); // Ensure exactly-once semantics
    }

    public static void main(String[] args) {
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "spliter-data-app");
        getProps(props);

        //TODO: add format file here using the HashMap

        // Configure producer
        producerProps.put("bootstrap.servers", "localhost:9092");
        producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put("acks", "all");  // Ensure that all replicas acknowledge the message
        producerProps.put("retries", 3);   // Retry on failure
        producerProps.put("enable.idempotence", "true");

        // Initialize the producer once
        producer = new KafkaProducer<>(producerProps);

        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, String> sensorStream = builder.stream(inputTopic);

        // Ensure the input topic exists before processing
        checkAndCreateTopic(inputTopic);

        // Process the sensor data and send it to the appropriate topic
        sensorStream.foreach((key, value) -> {
            try {
                JsonNode node = mapper.readTree(value);
                JsonNode data = node.get("data");
                String topicType = data.get("type").asText();
                String id = data.get("id").asText();

                // send all messages as is through the systems output
                producer.send(new ProducerRecord<>(outputTopic, id, value), (metadata, exception) -> {
                    if (exception != null) {
                        System.err.println("Failed to send message: " + exception.getMessage());
                        // Optionally, handle the failure (e.g., retry logic)
                    } else {
                        log.info("Message sent to output: " + value);
                    }
                });

                if (!node.has("data") || data.has("Error")) {
                    System.err.println("Invalid message format: 'data' field missing or Error. Sending message to errors: " + value);
                    producer.send(new ProducerRecord<>(errorTopic, id, value), (metadata, exception) -> {
                        if (exception != null) {
                            System.err.println("Failed to send message: " + exception.getMessage());
                            // Optionally, handle the failure (e.g., retry logic)
                        } else {
                            log.info("Message sent to errorTopic: " + value);
                        }
                    });
                    return;
                }

                // check the entries of their format
                if (!checkFormat(node)) {
                    System.err.println("Invalid message format: 'data' field missing or Error. Sending message to errors: " + value);
                    producer.send(new ProducerRecord<>(errorTopic, id, value), (metadata, exception) -> {
                        if (exception != null) {
                            System.err.println("Failed to send message: " + exception.getMessage());
                            // Optionally, handle the failure (e.g., retry logic)
                        } else {
                            log.info("Message sent to errorTopic due to wrong format: " + value);
                        }
                    });
                    return;
                }

                // Send the message to the appropriate topic
                producer.send(new ProducerRecord<>(topicType, id, value), (metadata, exception) -> {
                    if (exception != null) {
                        System.err.println("Failed to send message: " + exception.getMessage());
                    } else {
                        log.info("Message sent to topic " + topicType + ": " + value);
                    }
                });

                // Send the message to the appropriate topic for the group
                String groupTopic = topicType + "-group";
                producer.send(new ProducerRecord<>(groupTopic, id, value), (metadata, exception) -> {
                    if (exception != null) {
                        System.err.println("Failed to send message: " + exception.getMessage());
                        // Optionally, handle the failure (e.g., retry logic)
                    } else {
                        log.info("Message sent to topic " + groupTopic + ": " + value);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Start the Kafka Streams application
        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();

        // Graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    private static boolean checkFormat(JsonNode node) {
        boolean format = true;
        if(keywords.containsKey(node.get("type").asText())) {
            List<String> list = keywords.get(node.get("type").asText());
            for(String str : list) {
                if(!node.has(str)) {
                    format = false;
                    break;
                }
            }
        }
        return format;
    }

    /**
     * Check if the given topic exists. If not, create it.
     *
     * @param topic The name of the topic.
     */
    public static void checkAndCreateTopic(String topic) {
        if (!existingTopics.contains(topic)) {
            try (AdminClient adminClient = AdminClient.create(props)) {
                // Check if the topic exists
                if (!adminClient.listTopics().names().get().contains(topic)) {
                    // If not, create the topic
                    NewTopic newTopic = new NewTopic(topic, 1, (short) 1); // 1 partition, 1 replica
                    adminClient.createTopics(Collections.singleton(newTopic));
                    existingTopics.add(topic);
                    log.info("Topic created: " + topic);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
