package dev.kafka.streams;

import static dev.kafka.util.Connection.MAPPER;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.kafka.util.Connection;
import dev.kafka.util.TrackProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;

@Slf4j
public class WarningsStream {

    public static void run() {
        // Initialize the producer once
        TrackProducer<String, String> producer = Connection.getProducer( "warning-app" );

        StreamsBuilder builder = new StreamsBuilder();

        String inputTopic = "f1";
        String finalTopic = "errors";
        KStream<String, String> sensorStream = builder.stream( inputTopic );

        // Ensure the input topic exists before processing
        Connection.checkAndCreateTopic( producer.getProperties(), inputTopic );

        // Process the sensor data and send it to the appropriate topic
        sensorStream.foreach( ( key, value ) -> {
            try {
                JsonNode json = MAPPER.readTree( value );
                if ( !json.has( "data" ) || !(json.get( "data" ).isObject()) ) {
                    System.err.println( "Invalid message format: 'data' field missing or not a JSONObject. Skipping message: " + value );
                    return;
                }
                JsonNode data = json.get( "data" );
                if ( !data.has( "type" ) ) {
                    System.err.println( "Invalid message format: 'type' field missing in 'data'. Skipping message: " + value );
                    return;
                }

                String warning;
                JsonNode warningJson = MAPPER.readTree( value );
                String type = data.get( "type" ).asText();

                if ( warningJson.get( "data" ).has( "Error" ) ) {
                    producer.send( new ProducerRecord<>( finalTopic, key, warningJson.toString() ), ( metadata, exception ) -> {
                        if ( exception != null ) {
                            System.err.println( "Failed to send message: " + exception.getMessage() );
                            // Optionally, handle the failure (e.g., retry logic)
                        } else {
                            log.info( "Message sent to topic f3: " + value );
                        }
                    } );
                } else {
                    boolean send = false;
                    switch ( type ) {
                        case "tire":
                            if ( data.get( "temperature tire" ).intValue() > 110 ) {
                                warning = "position:" + data.get( "position" ).intValue() + " is to hot.";
                                warningJson = createErrorObject( json, type, warning );
                                send = true;
                            }
                            if ( data.get( "wear" ).intValue() > 90 ) {
                                warning = "position:" + data.get( "position" ).intValue() + " is worn down.";
                                warningJson = createErrorObject( json, type, warning );
                                send = true;
                            }
                            if ( data.get( "pressure psi" ).doubleValue() > 30 ) {
                                warning = "position:" + data.get( "position" ).intValue() + " to high pressure.";
                                warningJson = createErrorObject( json, type, warning );
                                send = true;
                            }
                            break;

                        case "heat":
                            if ( data.get( "temperature c" ).intValue() > 50 ) {
                                warning = " to hot temperature.";
                                warningJson = createErrorObject( json, type, warning );
                                send = true;
                            }
                            break;

                        case "engine":
                            if ( data.get( "oil_pressure" ).doubleValue() > 7 ) {
                                warning = " oil pressure to high.";
                                warningJson = createErrorObject( json, type, warning );
                                send = true;
                            }
                            if ( data.get( "temperature engine" ).intValue() > 600 ) {
                                warning = " is overheating.";
                                warningJson = createErrorObject( json, type, warning );
                                send = true;
                            }
                            if ( data.get( "fuel_pressure" ).doubleValue() > 5 ) {
                                warning = " fuel pressure to high.";
                                warningJson = createErrorObject( json, type, warning );
                                send = true;
                            }
                            if ( data.get( "rpm" ).longValue() > 18000 ) {
                                warning = " rpm to high.";
                                warningJson = createErrorObject( json, type, warning );
                                send = true;
                            }
                            if ( data.get( "exhaust" ).doubleValue() > 1.2 ) {
                                warning = " exhaust fumes not good.";
                                warningJson = createErrorObject( json, type, warning );
                                send = true;
                            }
                            if ( data.get( "fuelFlow" ).intValue() > 120 ) {
                                warning = " fuelFlow to high.";
                                warningJson = createErrorObject( json, type, warning );
                                send = true;
                            }
                            break;

                        case "fuelPump":
                            if ( data.get( "ml/min" ).longValue() > 4000 ) {
                                warning = " fuel flow is to low.";
                                warningJson = createErrorObject( json, type, warning );
                                send = true;
                            }
                            if ( data.get( "temperature fuelP" ).intValue() > 60 ) {
                                warning = " fuel-pump temperature is to high.";
                                warningJson = createErrorObject( json, type, warning );
                                send = true;
                            }
                            break;

                        case "brake":
                            if ( data.get( "temperature brake" ).intValue() > 1000 ) {
                                warning = " is overheating.";
                                warningJson = createErrorObject( json, type, warning );
                                send = true;
                            }
                            if ( data.get( "wear" ).intValue() > 90 ) {
                                warning = " is worn down.";
                                warningJson = createErrorObject( json, type, warning );
                                send = true;
                            }
                            if ( data.get( "brake_pressure" ).intValue() > 10 ) {
                                warning = " brake pressure to high.";
                                warningJson = createErrorObject( json, type, warning );
                                send = true;
                            }
                            break;

                        case "gForce":
                            if ( data.get( "g-lateral" ).doubleValue() > 6 ) {
                                warning = " g-force lateral is high.";
                                warningJson = createErrorObject( json, type, warning );
                                send = true;
                            } else if ( data.get( "g-longitudinal" ).doubleValue() > 5 ) {
                                warning = " g-force longitudinal is high.";
                                warningJson = createErrorObject( json, type, warning );
                                send = true;
                            }
                            break;
                        case "accelerometer":
                            if ( data.get( "throttlepedall" ).intValue() > 100 ) {
                                warning = " throttlepedall is high.";
                                warningJson = createErrorObject( json, type, warning );
                                send = true;
                            }
                            break;

                        case "speed":
                            if ( data.get( "wind speed" ).doubleValue() > 200 ) {
                                warning = " wind speed is to high.";
                                warningJson = createErrorObject( json, type, warning );
                                send = true;
                            }
                            if ( data.get( "kph" ).doubleValue() > 360 ) {
                                warning = " kph is high.";
                                warningJson = createErrorObject( json, type, warning );
                                send = true;
                            }
                            break;

                        default:
                            send = false;
                            break;
                    }
                    // Send the message
                    if ( send ) {
                        producer.send( new ProducerRecord<>( finalTopic, key, warningJson.toString() ), ( metadata, exception ) -> {
                            if ( exception != null ) {
                                System.err.println( "Failed to send message: " + exception.getMessage() );
                                // Optionally, handle the failure (e.g., retry logic)
                            } else {
                                log.info( "Message sent to topic f3: " + value );
                            }
                        } );
                    }
                }

            } catch ( Exception e ) {
                throw new RuntimeException( e );
            }
        } );

        // Start the Kafka Streams application
        KafkaStreams streams = new KafkaStreams( builder.build(), producer.getProperties() );
        streams.start();

        // Graceful shutdown
        Runtime.getRuntime().addShutdownHook( new Thread( streams::close ) );
    }


    public static JsonNode createErrorObject( JsonNode node, String type, String warning ) {
        ObjectNode error = JsonNodeFactory.instance.objectNode();
        error.putIfAbsent( "data", node.get( "data" ) );
        error.put( "WarningMessage", type + " id:" + node.get( "data" ).get( "id" ).intValue() + " " + warning );
        error.put( "tick", node.get( "tick" ).intValue() );
        error.put( "id", node.get( "id" ).intValue() );
        return error;
    }

}

