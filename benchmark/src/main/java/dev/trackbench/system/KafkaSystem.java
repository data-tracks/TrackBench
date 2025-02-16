package dev.trackbench.system;

import com.fasterxml.jackson.databind.JsonNode;
import dev.trackbench.Main;
import dev.trackbench.configuration.BenchmarkConfig;
import dev.trackbench.configuration.workloads.ErrorWorkload;
import dev.trackbench.configuration.workloads.IdentityWorkload;
import dev.trackbench.configuration.workloads.WindowGroupWorkload;
import dev.trackbench.configuration.workloads.Workload;
import dev.trackbench.display.Display;
import dev.trackbench.display.DisplayUtils;
import dev.trackbench.execution.receiver.Buffer;
import dev.trackbench.util.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.RoundRobinAssignor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

@Slf4j
public class KafkaSystem implements System {

    private static final String SETTINGS_FILE = "kafka.properties";


    private final PropertiesConfiguration configuration;

    @Setter
    private BenchmarkConfig config;

    private final List<Producer<String, String>> producers = new ArrayList<>();


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
        producers.add( producer );
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
        long timeout = config.executionMaxM() * 60 * toSecondsMultiplier();
        return () -> {
            Properties props = new Properties();
            props.put( ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, configuration.getProperty( "receiverUrl" ) );
            props.put( ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName() );
            props.put( ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName() );
            props.put( ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, RoundRobinAssignor.class.getName() );
            props.put( ConsumerConfig.GROUP_ID_CONFIG, String.format( "consumer-%s-app", workload.getName() ) );
            props.put( ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 1 ); // ms to wait until data is fetched
            props.put( ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest" );

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
                        if ( now - tick > timeout ) {
                            //Display.INSTANCE.info( "No messages received for {} seconds ({} ticks). Ending loop.", config.executionMaxM() * 60, DisplayUtils.printNumber( timeout ) );
                            running.set( false );

                            consumer.close();
                            dataConsumer.interrupt();
                        }
                    }

                }
            } catch ( Exception e ) {
                throw new RuntimeException( e );
            }
        };
    }


    @Override
    public void finish() {
        producers.forEach( Producer::close );
    }


    private long toSecondsMultiplier() {
        return 1_000_000_000 / config.stepDurationNs();
    }


    private String getTopic( Workload workload ) {
        if ( workload instanceof IdentityWorkload ) {
            return "f2";
        } else if ( workload instanceof WindowGroupWorkload ) {
            return "window";
        } else if ( workload instanceof ErrorWorkload ) {
            return "errors";
        } else if ( workload != null ) {
            return "mini-group";
        }
        throw new RuntimeException( "Unknown workload: " + null );
    }

}
