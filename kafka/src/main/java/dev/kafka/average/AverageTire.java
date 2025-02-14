package dev.kafka.average;

import lombok.Getter;

public class AverageTire {
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

    public AverageTire(double temp, double pressure, int count, int tickStart, int tickEnd, int id, int position, int wear) {
        this.temp = temp;
        this.pressure = pressure;
        this.count = count;
        this.tickStart = tickStart;
        this.tickEnd = tickEnd;
        this.id = id;
        this.position = position;
        this.wear = wear;
    }


    public double[] getAverage() {
        double[] average = new double[3];
        if (count != 0) {
            average[0] = temp / count;
            average[1] = pressure / count;
            average[2] = (double) wear / count;
        } else {
            return new double[]{temp, pressure, wear};
        }
//        Display.INSTANCE.info(average[0] + " " + average[1] + " " + average[2]);
        return average;
    }
}
