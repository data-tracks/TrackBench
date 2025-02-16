package dev.kafka.serialize;

import dev.kafka.average.AverageSpeedGroup;
import dev.kafka.util.SerdeUtil;
import dev.kafka.util.SerdeUtil.SerdeValues;
import java.nio.ByteBuffer;
import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class AverageSpeedGroupSerde implements Serde<AverageSpeedGroup> {

    @Override
    public Serializer<AverageSpeedGroup> serializer() {
        return new AverageSerializer();
    }


    @Override
    public Deserializer<AverageSpeedGroup> deserializer() {
        return new AverageDeserializer();
    }


    public static class AverageSerializer implements Serializer<AverageSpeedGroup> {

        @Override
        public byte[] serialize( String topic, AverageSpeedGroup data ) {
            if ( data == null ) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.allocate( 30_000 );
            SerdeUtil.addDefault( buffer, data );
            buffer.putDouble( data.speed );
            buffer.putDouble( data.wind );
            buffer.putDouble( data.minSpeed );
            buffer.putDouble( data.maxSpeed );
            buffer.putDouble( data.minWind );
            buffer.putDouble( data.maxWind );

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


    public static class AverageDeserializer implements Deserializer<AverageSpeedGroup> {

        @Override
        public AverageSpeedGroup deserialize( String topic, byte[] data ) {
            if ( data == null || data.length == 0 ) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.wrap( data );
            SerdeValues values = SerdeUtil.readDefault( buffer );
            double speed = buffer.getDouble();
            double wind = buffer.getDouble();
            double minSpeed = buffer.getDouble();
            double maxSpeed = buffer.getDouble();
            double minWind = buffer.getDouble();
            double maxWind = buffer.getDouble();
            return new AverageSpeedGroup( speed, wind, values );
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
