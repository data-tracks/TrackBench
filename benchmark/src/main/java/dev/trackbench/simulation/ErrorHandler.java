package dev.trackbench.simulation;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.trackbench.BenchmarkConfig;
import dev.trackbench.simulation.sensor.Sensor;
import java.io.IOException;

public class ErrorHandler {

    private final Sensor sensor;
    private final BenchmarkConfig config;

    // No data error
    private final long noDataErrorInterval;
    private final long maxNoDataAlteration;

    private long dynamicNoDataErrorInterval;
    private long noDataErrorCounter;

    // Drop error
    private final long dropErrorInterval;
    private final long maxDropAlteration;

    private long dynamicDropErrorInterval;
    private long dropErrorCounter;


    public ErrorHandler( Sensor sensor ) {
        this.sensor = sensor;
        this.config = sensor.getConfig();

        this.noDataErrorInterval = config.calculateErrorInterval( sensor, sensor.getTemplate().getErrorRates().getNoDataRate() );
        this.maxNoDataAlteration = (long) (this.noDataErrorInterval * config.maxErrorAlteration());
        this.noDataErrorCounter = sensor.random.nextLong( noDataErrorInterval );

        this.dropErrorInterval = config.calculateErrorInterval( sensor, sensor.getTemplate().getErrorRates().getNoDataRate() );
        this.maxDropAlteration = (long) (this.dropErrorInterval * config.maxErrorAlteration());
        this.dropErrorCounter = sensor.random.nextLong( dropErrorInterval );
    }


    public boolean handleError( long tick ) throws IOException {
        if ( isNoDataError() ) {
            sensor.getMetric().noDataErrors++;

            attachNoData( tick );
            noDataErrorCounter = 0;
            this.dynamicNoDataErrorInterval = recalculateError( this.noDataErrorInterval, this.maxNoDataAlteration );
        } else if ( isDropError() ) {
            sensor.getMetric().dropErrors++;

            // we drop and do nothing
            dropErrorCounter = 0;
            this.dynamicDropErrorInterval = recalculateError( this.dropErrorInterval, this.maxDropAlteration );
        } else {
            // no error
            noDataErrorCounter++;
            dropErrorCounter++;
            return false;
        }

        return true;
    }


    private boolean isNoDataError() {
        return noDataErrorCounter >= dynamicNoDataErrorInterval;
    }


    private boolean isDropError() {
        return dropErrorCounter >= dynamicDropErrorInterval;
    }


    private long recalculateError( long interval, long maxAlteration ) {
        // max shift of error in more or less interval between two errors
        return interval + (int) (sensor.random.nextFloat( -maxAlteration, maxAlteration ));
    }


    private void attachNoData( long tick ) throws IOException {
        ObjectNode errorData = JsonNodeFactory.instance.objectNode();
        errorData.put( "type", sensor.getTemplate().getType() );
        errorData.put( "id", sensor.id );
        errorData.put( "Error", "No Data" );

        ObjectNode error = JsonNodeFactory.instance.objectNode();
        error.put( "tick", tick );
        error.putIfAbsent( "data", errorData );

        sensor.getDataWithErrorTarget().attach( error );
        sensor.getErrorTarget().attach( error );
    }

}
