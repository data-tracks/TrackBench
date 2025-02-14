package dev.trackbench.system;

import com.fasterxml.jackson.databind.JsonNode;
import dev.trackbench.Main;
import dev.trackbench.configuration.workloads.ErrorWorkload;
import dev.trackbench.configuration.workloads.IdentityWorkload;
import dev.trackbench.configuration.workloads.WindowGroupWorkload;
import dev.trackbench.configuration.workloads.Workload;
import dev.trackbench.display.Display;
import dev.trackbench.execution.receiver.Buffer;
import dev.trackbench.util.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

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
        props.put( ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, configuration.getProperty( "senderUrl" ) );
        props.put( ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName() );
        props.put( ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName() );
        String topic = this.configuration.getString( "senderTopic" );

        KafkaProducer<String, String> producer = new KafkaProducer<>( props );
        return node -> {
            ProducerRecord<String, String> record = new ProducerRecord<>( topic, node.toString() );
            try {
                producer.send( record ).get();
            } catch ( InterruptedException | ExecutionException e ) {
                throw new RuntimeException( e );
            }
        };

    }


    @Override
    public Runnable getReceiver( Workload workload, AtomicBoolean running, AtomicBoolean ready, Clock clock, Buffer dataConsumer ) {
        return () -> {
            Properties props = new Properties();
            props.put( ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, configuration.getProperty( "receiverUrl" ) );
            props.put( ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName() );
            props.put( ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName() );
            props.put( "group.id", configuration.getString( "receiverGroupId" ) );
            props.put( "auto.offset.reset", "latest" ); // Start from the beginning of the topic if no offset is found

            KafkaConsumer<String, String> consumer = new KafkaConsumer<>( props );

            long tick = clock.tick();

            try ( consumer ) {
                consumer.subscribe( List.of( getTopic( workload ) ) );

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
                            Display.INSTANCE.info( "No messages received for 10 seconds. Ending loop." );
                            running.set( false );
                        }
                    }

                }
            } catch ( Exception e ) {
                throw new RuntimeException( e );
            }
        };
    }


    private String getTopic( Workload workload ) {
        if ( workload instanceof IdentityWorkload ) {
            return "f2";
        } else if ( workload instanceof WindowGroupWorkload ) {
            return "large-group";
        } else if ( workload instanceof ErrorWorkload ) {
            return "errors";
        } else if ( workload != null ) {
            return "small-group";
        }
        throw new RuntimeException( "Unknown workload: " + null );
    }

}
