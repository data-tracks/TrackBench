package dev.kafka.util;

import dev.kafka.average.Average;
import java.nio.ByteBuffer;

public class SerdeUtil {

    public static void addDefault( ByteBuffer buffer, Average average ) {
        buffer.putLong( average.count );
        buffer.putLong( average.tickStart );
        buffer.putLong( average.tickEnd );
        buffer.putLong( average.id );
        buffer.putLong( average.tick );
    }


    public static SerdeValues readDefault( ByteBuffer buffer ) {
        return new SerdeValues(
                buffer.getLong(),
                buffer.getLong(),
                buffer.getLong(),
                buffer.getLong(),
                buffer.getLong() );
    }


    public static record SerdeValues(long count, long tickStart, long tickEnd, long id, long tick) {

    }

}
