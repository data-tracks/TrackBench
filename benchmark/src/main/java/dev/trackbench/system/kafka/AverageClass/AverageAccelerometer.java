package dev.trackbench.system.kafka.AverageClass;

import lombok.Getter;

public class AverageAccelerometer {
    public double throttle;
    @Getter
    public int count;
    @Getter
    public int tickStart;
    @Getter
    public int tickEnd;
    @Getter
    public int id;


    public AverageAccelerometer(double throttle, int count, int tickStart, int tickEnd, int id) {
        this.throttle = throttle;
        this.count = count;
        this.tickStart = tickStart;
        this.tickEnd = tickEnd;
        this.id = id;
    }

    public double getTemp() {
        return throttle;
    }


    public double getAverage() {
        double average;
        if(count != 0) {
            average = throttle/count;
        } else {
            return throttle;
        }
//        log.info(average[0] + " " + average[1] + " " + average[2]);
        return average;
    }
}
