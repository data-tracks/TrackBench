package dev.kafka.serialize;

import dev.kafka.average.AverageAccelerometer;
import dev.kafka.util.SerdeUtil;
import dev.kafka.util.SerdeUtil.SerdeValues;
import java.nio.ByteBuffer;
import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class AverageAccelerometerSerde implements Serde<AverageAccelerometer> {

    @Override
    public Serializer<AverageAccelerometer> serializer() {
        return new AverageSerializer();
    }


    @Override
    public Deserializer<AverageAccelerometer> deserializer() {
        return new AverageDeserializer();
    }


    public static class AverageSerializer implements Serializer<AverageAccelerometer> {

        @Override
        public byte[] serialize( String topic, AverageAccelerometer data ) {
            if ( data == null ) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.allocate( 100_000 );
            SerdeUtil.addDefault( buffer, data );
            buffer.putDouble( data.throttle );

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


    public static class AverageDeserializer implements Deserializer<AverageAccelerometer> {

        @Override
        public AverageAccelerometer deserialize( String topic, byte[] data ) {
            if ( data == null || data.length == 0 ) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.wrap( data );
            SerdeValues values = SerdeUtil.readDefault( buffer );

            double throttle = buffer.getDouble();
            return new AverageAccelerometer( throttle, values );
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
