package dev.kafka.average;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.kafka.sensor.Sensor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
public abstract class Average {

    public long count;
    public long tickStart;
    public long tickEnd;
    public long tick;
    public long id;


    public Average() {
        count = 0;
        tickStart = 0;
        tickEnd = 0;
        id = -1;
    }

    public abstract ProducerRecord<String, String> getRecord( String topic );


    protected void update( Sensor entry ) {
        count += 1;
        if ( tickEnd < entry.tick ) {
            tickEnd = entry.tick;
        }
        if ( id == -1 ) {
            id = entry.id;
            tickStart = entry.tick;
        }
    }


    public abstract void next( Sensor entry );


    public void nextValue( Sensor entry ) {
        if ( !entry.error ) {
            next( entry );
            update( entry );
        }
    }


    @NotNull
    protected ProducerRecord<String, String> wrapRecord( String key, String topic, ObjectNode data ) {
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        json.put( "id", id );
        json.put( "type", key );
        json.put( "tick", tickEnd );
        //json.put( "startTime", tickStart );
        //json.put( "endTime", tickEnd );
        json.putIfAbsent( "data", data );

        String jsonMessage = json.toString();
        return new ProducerRecord<>( topic, key, jsonMessage );
    }


}
