package dev.kafka.serialize;

import dev.kafka.average.AverageTire;
import dev.kafka.util.SerdeUtil;
import dev.kafka.util.SerdeUtil.SerdeValues;
import java.nio.ByteBuffer;
import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class AverageTireSerde implements Serde<AverageTire> {

    @Override
    public Serializer<AverageTire> serializer() {
        return new AverageSerializer();
    }


    @Override
    public Deserializer<AverageTire> deserializer() {
        return new AverageDeserializer();
    }


    public static class AverageSerializer implements Serializer<AverageTire> {

        @Override
        public byte[] serialize( String topic, AverageTire data ) {
            if ( data == null ) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.allocate( 60_000 );
            SerdeUtil.addDefault( buffer, data );
            buffer.putDouble( data.temp );
            buffer.putDouble( data.pressure );
            buffer.putInt( data.position );
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


    public static class AverageDeserializer implements Deserializer<AverageTire> {

        @Override
        public AverageTire deserialize( String topic, byte[] data ) {
            if ( data == null || data.length == 0 ) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.wrap( data );
            SerdeValues values = SerdeUtil.readDefault( buffer );
            double temp = buffer.getDouble();
            double pressure = buffer.getDouble();
            int position = buffer.getInt();
            int wear = buffer.getInt();
            return new AverageTire( temp, pressure, position, wear, values );
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
