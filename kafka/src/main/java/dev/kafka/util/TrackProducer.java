package dev.kafka.util;

import java.util.Properties;
import lombok.Getter;
import org.apache.kafka.clients.producer.KafkaProducer;

@Getter
public final class TrackProducer<T, K> extends KafkaProducer<T, K> {

    private final Properties properties;


    public TrackProducer( Properties properties ) {
        super( properties );
        this.properties = properties;
    }


}
