package dev.kafka.average;

import lombok.Getter;

@Getter
public class AverageHeat {
    public double temp;
    public int count;
    public int tickStart;
    public int tickEnd;
    public int id;


    public AverageHeat(double temp, int count, int tickStart, int tickEnd, int id) {
        this.temp = temp;
        this.count = count;
        this.tickStart = tickStart;
        this.tickEnd = tickEnd;
        this.id = id;
    }


    public double getAverage() {
        double average;
        if (count != 0) {
            average = temp / count;
        } else {
            return temp;
        }
//        Display.INSTANCE.info(average[0] + " " + average[1] + " " + average[2]);
        return average;
    }
}
