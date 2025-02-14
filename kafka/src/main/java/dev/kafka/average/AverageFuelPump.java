package dev.kafka.average;

import lombok.Getter;

public class AverageFuelPump {
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

    public AverageFuelPump(double temp, double flowRate, int count, int tickStart, int tickEnd, int id) {
        this.temp = temp;
        this.flowRate = flowRate;
        this.count = count;
        this.tickStart = tickStart;
        this.tickEnd = tickEnd;
        this.id = id;
    }


    public double getPressure() {
        return flowRate;
    }


    public double[] getAverage() {
        double[] average = new double[2];
        if (count != 0) {
            average[0] = temp / count;
            average[1] = flowRate / count;
        } else {
            return new double[]{temp, flowRate};
        }
//        Display.INSTANCE.info(average[0] + " " + average[1] + " " + average[2]);
        return average;
    }
}
