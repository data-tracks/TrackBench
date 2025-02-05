package dev.trackbench.system.kafka.AverageClass;

import lombok.Getter;

public class AverageEngine {
    public int temp;
    @Getter
    public long rpm;
    public int fuelFlow;
    public double oilPressure;
    public double fuelPressure;
    public double exhaust;
    @Getter
    public int count;
    @Getter
    public int tickStart;
    @Getter
    public int tickEnd;
    @Getter
    public int id;

    public AverageEngine(int temp, long rpm,int fuelFlow, double oilPressure, double fuelPressure,
                         double exhaust, int count, int tickStart, int tickEnd, int id) {
        this.temp = temp;
        this.rpm = rpm;
        this.count = count;
        this.tickStart = tickStart;
        this.tickEnd = tickEnd;
        this.id = id;
        this.fuelFlow = fuelFlow;
        this.oilPressure = oilPressure;
        this.fuelPressure = fuelPressure;
        this.exhaust = exhaust;
    }

    public double getTemp() {
        return temp;
    }


    public double[] getAverage() {
        double[] average = new double[6];
        if(count != 0) {
            if(temp != 0) {
                average[0] = (double) temp/count;
            }
            if(rpm != 0) {
                average[1] = (double) rpm /count;
            }
            if(fuelFlow!= 0) {
                average[2] = (double) fuelFlow/count;
            }
            if(oilPressure != 0) {
                average[3] = oilPressure/count;
            }
            if(fuelPressure != 0) {
                average[4] = fuelPressure/count;
            }
            if(exhaust != 0) {
                average[5] = exhaust/count;
            }
        } else {
            return new double[]{temp,(double) rpm, oilPressure, fuelPressure, exhaust, id};
        }
//        Display.INSTANCE.info(average[0] + " " + average[1] + " " + average[2] + " " + average[3] + " " + average[4] + " " + average[5]);
        return average;
    }
}
