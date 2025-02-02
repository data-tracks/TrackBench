package dev.trackbench.system.kafka.AverageClass;

import lombok.Getter;

@Getter
public class AverageHeatGroup {
    public double temp;
    public int count;
    public int tickStart;
    public int tickEnd;
    public int id;
    public double minTemp;
    public double maxTemp;


    public AverageHeatGroup(double temp, int count, int tickStart, int tickEnd, int id) {
        this.temp = temp;
        this.count = count;
        this.tickStart = tickStart;
        this.tickEnd = tickEnd;
        this.id = id;
        this.minTemp = Double.MAX_VALUE;
        this.maxTemp = Double.MIN_VALUE;
    }


    public double getAverage() {
        double average;
        if(count != 0) {
            average = temp/count;
        } else {
            return temp;
        }
//        log.info(average[0] + " " + average[1] + " " + average[2]);
        return average;
    }
}
