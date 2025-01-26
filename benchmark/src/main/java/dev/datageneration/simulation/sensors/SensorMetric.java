package dev.datageneration.simulation.sensors;


public class SensorMetric {
    public long ticksGenerated = 0;
    public long dropErrors = 0;
    public long noDataErrors = 0;


    @Override
    public String toString() {
        return "{ticksGenerated:%d, dropErrors:%d, noDataErrors:%d}".formatted( ticksGenerated, dropErrors, noDataErrors );
    }

}
