package dev.kafka.serialize;

import dev.kafka.average.AverageFuelPump;
import dev.kafka.util.SerdeUtil;
import dev.kafka.util.SerdeUtil.SerdeValues;
import java.nio.ByteBuffer;
import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class AverageFuelPumpSerde implements Serde<AverageFuelPump> {

    @Override
    public Serializer<AverageFuelPump> serializer() {
        return new AverageSerializer();
    }


    @Override
    public Deserializer<AverageFuelPump> deserializer() {
        return new AverageDeserializer();
    }


    public static class AverageSerializer implements Serializer<AverageFuelPump> {

        @Override
        public byte[] serialize( String topic, AverageFuelPump data ) {
            if ( data == null ) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.allocate( 50_000 );
            SerdeUtil.addDefault( buffer, data );
            buffer.putDouble( data.temp );
            buffer.putDouble( data.flowRate );

            return buffer.array();
        }


        @Override
        public void configure( Map<String, ?> configs, boolean isKey ) {
            // No configuration needed
        }


        @Override
        public void close() {
            // No resources to close
        }

    }


    public static class AverageDeserializer implements Deserializer<AverageFuelPump> {

        @Override
        public AverageFuelPump deserialize( String topic, byte[] data ) {
            if ( data == null || data.length == 0 ) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.wrap( data );
            SerdeValues values = SerdeUtil.readDefault( buffer );
            double temp = buffer.getDouble();
            double flowRate = buffer.getDouble();

            return new AverageFuelPump( temp, flowRate, values );
        }


        @Override
        public void configure( Map<String, ?> configs, boolean isKey ) {
            // No configuration needed
        }


        @Override
        public void close() {
            // No resources to close
        }

    }

}
