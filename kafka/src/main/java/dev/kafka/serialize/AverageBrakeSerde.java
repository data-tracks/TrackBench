package dev.kafka.serialize;

import dev.kafka.average.AverageBrake;
import dev.kafka.util.SerdeUtil;
import dev.kafka.util.SerdeUtil.SerdeValues;
import java.nio.ByteBuffer;
import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class AverageBrakeSerde implements Serde<AverageBrake> {

    @Override
    public Serializer<AverageBrake> serializer() {
        return new AverageSerializer();
    }


    @Override
    public Deserializer<AverageBrake> deserializer() {
        return new AverageDeserializer();
    }


    public static class AverageSerializer implements Serializer<AverageBrake> {

        @Override
        public byte[] serialize( String topic, AverageBrake data ) {
            if ( data == null ) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.allocate( 100_000 );
            SerdeUtil.addDefault( buffer, data );
            buffer.putInt( data.temp );
            buffer.putInt( data.pressure );
            buffer.putLong( data.count );
            buffer.putInt( data.wear );

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


    public static class AverageDeserializer implements Deserializer<AverageBrake> {

        @Override
        public AverageBrake deserialize( String topic, byte[] data ) {
            if ( data == null || data.length == 0 ) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.wrap( data );
            SerdeValues values = SerdeUtil.readDefault( buffer );
            int temp = buffer.getInt();
            int pressure = buffer.getInt();
            int wear = buffer.getInt();
            return new AverageBrake( temp, pressure, wear, values );
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
