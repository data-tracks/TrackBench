package dev.kafka.serialize;

import dev.kafka.average.AverageSpeed;
import dev.kafka.util.SerdeUtil;
import dev.kafka.util.SerdeUtil.SerdeValues;
import java.nio.ByteBuffer;
import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class AverageSpeedSerde implements Serde<AverageSpeed> {

    @Override
    public Serializer<AverageSpeed> serializer() {
        return new AverageSerializer();
    }


    @Override
    public Deserializer<AverageSpeed> deserializer() {
        return new AverageDeserializer();
    }


    public static class AverageSerializer implements Serializer<AverageSpeed> {

        @Override
        public byte[] serialize( String topic, AverageSpeed data ) {
            if ( data == null ) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.allocate( 20_000 );
            SerdeUtil.addDefault( buffer, data );
            buffer.putDouble( data.speed );
            buffer.putDouble( data.wind );

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


    public static class AverageDeserializer implements Deserializer<AverageSpeed> {

        @Override
        public AverageSpeed deserialize( String topic, byte[] data ) {
            if ( data == null || data.length == 0 ) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.wrap( data );
            SerdeValues values = SerdeUtil.readDefault( buffer );
            double speed = buffer.getDouble();
            double wind = buffer.getDouble();
            return new AverageSpeed( speed, wind, values );
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
