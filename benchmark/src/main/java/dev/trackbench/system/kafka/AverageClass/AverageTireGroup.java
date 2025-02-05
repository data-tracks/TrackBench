package dev.trackbench.system.kafka.AverageClass;

import lombok.Getter;

public class AverageTireGroup {
    @Getter
    public double temp;
    @Getter
    public double pressure;
    @Getter
    public int count;
    @Getter
    public int tickStart;
    @Getter
    public int tickEnd;
    @Getter
    public int id;
    @Getter
    public int position;
    public int wear;
    @Getter
    public double minTemp;
    @Getter
    public double maxTemp;
    @Getter
    public double minPressure;
    @Getter
    public double maxPressure;

    public AverageTireGroup(double temp, double pressure, int count, int tickStart, int tickEnd, int id, int position, int wear) {
        this.temp = temp;
        this.pressure = pressure;
        this.count = count;
        this.tickStart = tickStart;
        this.tickEnd = tickEnd;
        this.id = id;
        this.position = position;
        this.wear = wear;
        this.minTemp = Double.MAX_VALUE;
        this.maxTemp = Double.MIN_VALUE;
        this.minPressure = Double.MAX_VALUE;
        this.maxPressure = Double.MIN_VALUE;
    }


    public double[] getAverage() {
        double[] average = new double[3];
        if(count != 0) {
            average[0] = temp/count;
            average[1] = pressure/count;
            average[2] = (double)wear/count;
        } else {
            return new double[]{temp, pressure, wear};
        }
//        Display.INSTANCE.info(average[0] + " " + average[1] + " " + average[2]);
        return average;
    }
}

