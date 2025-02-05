package dev.trackbench.system.kafka.AverageClass;

import lombok.Getter;

public class AverageSpeedGroup {
    public double speed;
    public double wind;
    @Getter
    public int count;
    @Getter
    public int tickStart;
    @Getter
    public int tickEnd;
    @Getter
    public int id;
    public double minSpeed;
    public double maxSpeed;
    public double minWind;
    public double maxWind;

    public AverageSpeedGroup(double speed, double wind, int count, int tickStart, int tickEnd, int id) {
        this.speed = speed;
        this.wind = wind;
        this.count = count;
        this.tickStart = tickStart;
        this.tickEnd = tickEnd;
        this.id = id;
        this.minSpeed = Double.MAX_VALUE;
        this.maxSpeed = Double.MIN_VALUE;
        this.minWind = Double.MAX_VALUE;
        this.maxWind = Double.MIN_VALUE;
    }

    public double getTemp() {
        return speed;
    }

    public double getPressure() {
        return wind;
    }


    public double[] getAverage() {
        double[] average = new double[2];
        if(count != 0) {
            average[0] = speed/count;
            average[1] = wind/count;
        } else {
            return new double[]{speed, wind};
        }
//        Display.INSTANCE.info(average[0] + " " + average[1] + " " + average[2]);
        return average;
    }
}
