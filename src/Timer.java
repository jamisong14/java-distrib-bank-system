package src;

import java.util.ArrayList;

class Timer {
    public long startTime;
    public long endTime;
    private ArrayList<Double> entries;

    public Timer() {
        this.startTime = 0;
        this.endTime = 0;
        this.entries = new ArrayList<Double>();
    }

    public void start() {
        this.startTime = System.nanoTime();
    }

    public void stop() {
        this.endTime = System.nanoTime();
        this.entries.add(getTime());
    }

    public void clear() {
        this.startTime = 0;
        this.endTime = 0;
    }

    public double getAverage() {
        double sum = 0;
        for (double entry : this.entries) {
            sum += entry;
        }
        return round(sum / this.entries.size(), 4);
    }

    public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
    
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public double getTime() {
        return round(((double) (this.endTime - this.startTime)) / 1_000_000_000.0, 4);
    }

}