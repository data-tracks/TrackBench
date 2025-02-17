package dev.kafka.serialize;

import dev.kafka.average.AverageBrakeGroup;
import dev.kafka.util.SerdeUtil;
import dev.kafka.util.SerdeUtil.SerdeValues;
import java.nio.ByteBuffer;
import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class AverageBrakeGroupSerde implements Serde<AverageBrakeGroup> {

    @Override
    public Serializer<AverageBrakeGroup> serializer() {
        return new AverageSerializer();
    }


    @Override
    public Deserializer<AverageBrakeGroup> deserializer() {
        return new AverageDeserializer();
    }


    public static class AverageSerializer implements Serializer<AverageBrakeGroup> {

        @Override
        public byte[] serialize( String topic, AverageBrakeGroup data ) {
            if ( data == null ) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.allocate( 100_000 );
            SerdeUtil.addDefault( buffer, data );
            buffer.putInt( data.temp );
            buffer.putInt( data.pressure );
            buffer.putInt( data.wear );
            buffer.putInt( data.minTemp );
            buffer.putInt( data.maxTemp );
            buffer.putInt( data.minPressure );
            buffer.putInt( data.maxPressure );

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


    public static class AverageDeserializer implements Deserializer<AverageBrakeGroup> {

        @Override
        public AverageBrakeGroup deserialize( String topic, byte[] data ) {
            if ( data == null || data.length == 0 ) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.wrap( data );
            SerdeValues values = SerdeUtil.readDefault( buffer );
            int temp = buffer.getInt();
            int pressure = buffer.getInt();
            int wear = buffer.getInt();
            return new AverageBrakeGroup( temp, pressure, wear, values );
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
