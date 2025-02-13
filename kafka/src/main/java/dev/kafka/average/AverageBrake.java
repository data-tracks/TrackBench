package dev.kafka.average;

import lombok.Getter;

public class AverageBrake {
    @Getter
    public int temp;
    @Getter
    public int pressure;
    @Getter
    public int count;
    @Getter
    public int tickStart;
    @Getter
    public int tickEnd;
    @Getter
    public int id;
    public int wear;

    public AverageBrake(int temp, int pressure, int count, int tickStart, int tickEnd, int id, int wear) {
        this.temp = temp;
        this.pressure = pressure;
        this.count = count;
        this.tickStart = tickStart;
        this.tickEnd = tickEnd;
        this.id = id;
        this.wear = wear;
    }


    public double[] getAverage() {
        double[] average = new double[]{0, 0, 0};
        if (count != 0) {
            if (temp != 0) {
                average[0] = (double) temp / count;
            }
            if (pressure != 0) {
                average[1] = (double) pressure / count;
            }
            if (wear != 0) {
                average[2] = (double) wear / count;
            }
        } else {
            return new double[]{temp, pressure, wear};
        }
//        Display.INSTANCE.info(average[0] + " " + average[1] + " " + average[2]);
        return average;
    }
}
