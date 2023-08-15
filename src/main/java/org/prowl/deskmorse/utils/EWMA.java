package org.prowl.deskmorse.utils;

/**
 * Moving average calculator
 */
public class EWMA {

    private double alpha;
    private double average = 0;

    public EWMA(double alpha) {
        this.alpha = alpha;
    }

    public double getAverage(double value) {
        average = alpha * value + (1 - alpha) * average;
        return average;
    }

}
