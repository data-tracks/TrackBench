package dev.kafka.average;

import lombok.Getter;

public class AverageEngineGroup {
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
    public long maxRPM;
    public long minRPM;
    public double minOilP;
    public double maxOilP;
    public double minExhaust;
    public double maxExhaust;
    public double minFuelP;
    public double maxFuelP;

    public AverageEngineGroup(int temp, long rpm, int fuelFlow, double oilPressure, double fuelPressure,
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
        this.maxRPM = Long.MAX_VALUE;
        this.minRPM = Long.MIN_VALUE;
        this.maxOilP = Double.MAX_VALUE;
        this.minOilP = Double.MIN_VALUE;
        this.minExhaust = Double.MAX_VALUE;
        this.maxExhaust = Double.MIN_VALUE;
        this.minFuelP = Double.MAX_VALUE;
        this.maxFuelP = Double.MIN_VALUE;
    }

    public double getTemp() {
        return temp;
    }


    public double[] getAverage() {
        double[] average = new double[6];
        if (count != 0) {
            if (temp != 0) {
                average[0] = (double) temp / count;
            }
            if (rpm != 0) {
                average[1] = (double) rpm / count;
            }
            if (fuelFlow != 0) {
                average[2] = (double) fuelFlow / count;
            }
            if (oilPressure != 0) {
                average[3] = oilPressure / count;
            }
            if (fuelPressure != 0) {
                average[4] = fuelPressure / count;
            }
            if (exhaust != 0) {
                average[5] = exhaust / count;
            }
        } else {
            return new double[]{temp, (double) rpm, oilPressure, fuelPressure, exhaust, id};
        }
//        Display.INSTANCE.info(average[0] + " " + average[1] + " " + average[2] + " " + average[3] + " " + average[4] + " " + average[5]);
        return average;
    }
}
