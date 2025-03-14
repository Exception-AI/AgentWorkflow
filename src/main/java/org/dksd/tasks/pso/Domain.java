package org.dksd.tasks.pso;

public class Domain {
    private double low;
    private double high;

    public Domain(double low, double high) {
        this.low = low;
        this.high = high;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }
}
