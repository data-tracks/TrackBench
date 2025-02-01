package dev.trackbench.simulation.error;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.trackbench.configuration.BenchmarkConfig;
import dev.trackbench.simulation.sensor.Sensor;
import java.io.IOException;
import java.util.List;
import lombok.Getter;

public class ErrorHandler {

    private final Sensor sensor;
    private final BenchmarkConfig config;
    @Getter
    private final List<Error> errors;


    public ErrorHandler( Sensor sensor ) {
        this.sensor = sensor;
        this.config = sensor.getConfig();

        this.errors = List.of(
                new NoDataError( sensor,
                        config.calculateErrorInterval(
                        sensor,
                        sensor.getTemplate().getErrorRates().getNoDataRate() ),
                        config.maxErrorAlteration(),
                        sensor.random,
                        sensor.getDataWithErrorTarget(),
                        sensor.getErrorTarget()
                ),
                new DropError( sensor,
                        config.calculateErrorInterval(
                        sensor,
                        sensor.getTemplate().getErrorRates().getDropRate() ),
                        config.maxErrorAlteration(),
                        sensor.random,
                        sensor.getDataWithErrorTarget(),
                        sensor.getErrorTarget()
                ));
    }


    public boolean handleError( long tick, ObjectNode data ) throws IOException {
        for ( Error error : errors ) {
            if ( error.injectError( tick, List.of(), data ) ){
                return true;
            }
        }
        return false;
    }

}
