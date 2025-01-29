package dev.trackbench.receiver;

import dev.trackbench.BenchmarkContext;
import dev.trackbench.util.FileJsonTarget;
import dev.trackbench.workloads.Workload;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReceiveCoordinator extends Thread {

    public final BenchmarkContext context;

    @Getter
    List<Receiver> receivers = new ArrayList<>();


    public ReceiveCoordinator( BenchmarkContext context ) {
        this.context = context;
    }


    @Override
    public void run() {
        prepare();
    }


    public void prepare() {
        for ( Entry<Integer, Workload> entry : context.getWorkloads().entrySet() ) {
            for ( long i = 0; i < context.getConfig().receivers(); i++ ) {
                Receiver receiver = new Receiver( entry.getValue(), context.getSystem(), context.getClock(), new FileJsonTarget( context.getConfig().getResultFile( entry.getValue().getName(), i ), context.getConfig() ) );
                receivers.add( receiver );
                receiver.start();
            }
        }

        try {
            for ( Receiver receiver : receivers ) {
                receiver.join();
            }
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }

    }


    public boolean allReady() {
        return receivers.stream().allMatch( r -> r.ready.get() );
    }


    public void stopReceivers() {
        for ( Receiver receiver : this.receivers ) {
            receiver.interrupt();
        }
    }


    /*public static void receive(boolean aggregated) throws IOException {
        props.put("bootstrap.servers", KafkaBroker);
        props.put("group.id", groupID);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "latest"); // Start from the beginning of the topic if no offset is found

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        try ( consumer ) {
            consumer.subscribe( Collections.singletonList( topic ) );
            inactivity = System.currentTimeMillis();
            boolean b = true;
            while ( b ) {
                ConsumerRecords<String, String> records = consumer.poll( Duration.ofMillis( 100 ) );
                if ( !records.isEmpty() ) {
                    for ( ConsumerRecord<String, String> record : records ) {
                        if ( !record.value().isEmpty() ) {
                            String recordValue = record.value();
                            JSONObject json = new JSONObject( recordValue );
                            dataReceived.add( json );
                            if ( throughput == 0 ) {
                                startTime = System.currentTimeMillis();
                            }
                            throughput++;
                            lastReceivedTime = System.currentTimeMillis();
                        }

                    }
                    inactivity = System.currentTimeMillis();

                } else {
                    long now = System.currentTimeMillis();
                    if ( now - inactivity > 10000 ) {
                        log.info( "No messages received for 10 seconds. Ending loop." );
                        b = false;
                    }
                }

            }
        } catch ( Exception e ) {
            log.error( e.getMessage() );
        }
        // Close the consumer
        Analyser.analyser(aggregated, dataReceived, startTime, lastReceivedTime, throughput);
    }*/
}