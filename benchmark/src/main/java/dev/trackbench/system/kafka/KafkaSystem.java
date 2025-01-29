package dev.trackbench.system.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import dev.trackbench.Main;
import dev.trackbench.receiver.Buffer;
import dev.trackbench.system.System;
import dev.trackbench.util.Clock;
import dev.trackbench.workloads.Workload;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

@Slf4j
public class KafkaSystem implements System {

    private static final String SETTINGS_FILE = "kafka.properties";


    private final PropertiesConfiguration configuration;


    public KafkaSystem() {
        Configurations configs = new Configurations();
        try {
            this.configuration = configs.properties( Main.class.getClassLoader().getResource( SETTINGS_FILE ) );
        } catch ( ConfigurationException e ) {
            throw new RuntimeException( e );
        }

    }


    @Override
    public void prepare() {

    }


    @Override
    public Consumer<JsonNode> getSender() {
        Properties props = new Properties();
        props.put( "bootstrap.servers", configuration.getProperty( "senderUrl" ) );
        props.put( "key.serializer", "org.apache.kafka.common.serialization.StringSerializer" );
        props.put( "value.serializer", "org.apache.kafka.common.serialization.StringSerializer" );
        String topic = this.configuration.getString( "senderTopic" );

        try ( KafkaProducer<String, String> producer = new KafkaProducer<>( props ) ) {
            return node -> {
                ProducerRecord<String, String> record = new ProducerRecord<>( topic, node.toString() );
                producer.send( record );
            };
        }

    }


    @Override
    public Runnable getReceiver( Workload workload, AtomicBoolean running, AtomicBoolean ready, Clock clock, Buffer dataConsumer ) {
        return () -> {
            Properties props = new Properties();
            props.put( "bootstrap.servers", configuration.getProperty( "receiverUrl" ) );
            props.put( "group.id", configuration.getString( "receiverGroup" ) );
            props.put( "key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer" );
            props.put( "value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer" );
            props.put( "auto.offset.reset", "latest" ); // Start from the beginning of the topic if no offset is found

            KafkaConsumer<String, String> consumer = new KafkaConsumer<>( props );

            long tick = clock.tick();

            try ( consumer ) {
                consumer.subscribe( List.of( workload.getName() ) );

                ready.set( true );

                while ( running.get() ) {
                    ConsumerRecords<String, String> records = consumer.poll( Duration.ofMillis( 100 ) );
                    if ( !records.isEmpty() ) {
                        tick = clock.tick();
                        for ( ConsumerRecord<String, String> record : records ) {
                            if ( !record.value().isEmpty() ) {
                                dataConsumer.attach( tick, record.value() );
                            }
                        }
                        tick = clock.tick();

                    } else {
                        long now = clock.tick();
                        if ( now - tick > 10000 ) {
                            log.info( "No messages received for 10 seconds. Ending loop." );
                            running.set( false );
                        }
                    }

                }
            }
        };
    }

}
