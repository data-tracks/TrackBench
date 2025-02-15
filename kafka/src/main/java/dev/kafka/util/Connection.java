package dev.kafka.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.Properties;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.jetbrains.annotations.NotNull;

public class Connection {

    public static final ObjectMapper MAPPER = new ObjectMapper();


    public static Properties getProps( String id ) {
        Properties props = new Properties();
        props.put( StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092" );
        props.put( StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass() );
        props.put( StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass() );
        props.put( ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest" );
        props.put( StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0 );
        props.put( StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2 ); // Ensure exactly-once semantics
        props.put( StreamsConfig.APPLICATION_ID_CONFIG, id );
        return props;
    }


    public static @NotNull TrackProducer<String, String> getProducer( String id ) {
        Properties props = getProps( id );
        // Configure producer
        props.put( "key.serializer", StringSerializer.class.getName() );
        props.put( "value.serializer", StringSerializer.class.getName() );
        //props.put( ProducerConfig.ACKS_CONFIG, "all" );  // Ensure that all replicas acknowledge the message
        props.put( ProducerConfig.RETRIES_CONFIG, 3 );   // Retry on failure
        props.put( ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 5 ); // ms to wait until data is fetched
        //props.put( "enable.idempotence", "true" );

        // Initialize the producer once
        return new TrackProducer<>( props );
    }


    /**
     * Check if the given topic exists. If not, create it.
     *
     * @param topic The name of the topic.
     */
    public static void checkAndCreateTopic( Properties properties, String topic ) {

        try ( AdminClient adminClient = AdminClient.create( properties ) ) {
            // Check if the topic exists
            if ( !adminClient.listTopics().names().get().contains( topic ) ) {
                // If not, create the topic
                NewTopic newTopic = new NewTopic( topic, 1, (short) 1 ); // 1 partition, 1 replica
                adminClient.createTopics( Collections.singleton( newTopic ) );
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

}
