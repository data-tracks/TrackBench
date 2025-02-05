package dev.trackbench.system.kafka.AverageClass;

import lombok.Getter;

public class AverageFuelPumpGroup {
    @Getter
    public double temp;
    public double flowRate;
    @Getter
    public int count;
    @Getter
    public int tickStart;
    @Getter
    public int tickEnd;
    @Getter
    public int id;
    public double maxTemp;
    public double minTemp;
    public double maxFlow;
    public double minFlow;

    public AverageFuelPumpGroup(double temp, double flowRate, int count, int tickStart, int tickEnd, int id) {
        this.temp = temp;
        this.flowRate = flowRate;
        this.count = count;
        this.tickStart = tickStart;
        this.tickEnd = tickEnd;
        this.id = id;
        this.maxTemp = Double.MAX_VALUE;
        this.minTemp = Double.MIN_VALUE;
        this.maxFlow = Double.MAX_VALUE;
        this.minFlow = Double.MIN_VALUE;
    }


    public double getPressure() {
        return flowRate;
    }


    public double[] getAverage() {
        double[] average = new double[2];
        if(count != 0) {
            average[0] = temp/count;
            average[1] = flowRate/count;
        } else {
            return new double[]{temp, flowRate};
        }
//        Display.INSTANCE.info(average[0] + " " + average[1] + " " + average[2]);
        return average;
    }
}
