/**
 * @file src/Timer.java
 * @brief Simple Timer class to measure time between request and response as observed by the client and each server.
 * @created 2024-03-30
 * @author Jamison Grudem (grude013)
 * 
 * @grace_days Using 1 grace day
 */

package src;

import java.util.ArrayList;

class Timer {
    // Start time
    public long startTime;
    // End time
    public long endTime;
    // Store timer entries here
    private ArrayList<Double> entries;

    /**
     * Initialize a new Timer
     */
    public Timer() {
        this.startTime = 0;
        this.endTime = 0;
        this.entries = new ArrayList<Double>();
    }

    /**
     * Start the timer
     */
    public void start() {
        this.startTime = System.nanoTime();
    }

    /**
     * Stop the timer
     */
    public void stop() {
        this.endTime = System.nanoTime();
        this.entries.add(getTime());
    }

    /**
     * Reset the timer to initial values
     */
    public void clear() {
        this.startTime = 0;
        this.endTime = 0;
    }

    /**
     * Get the average time of all entries
     * @return [double] The average time
     */
    public double getAverage() {
        double sum = 0;
        for (double entry : this.entries) {
            sum += entry;
        }
        return round(sum / this.entries.size(), 4);
    }

    /**
     * Round a double to a specified number of decimal places
     * @param value The number to round
     * @param places The places to round to
     * @return [double] The rounded number
     */
    public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
    
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    /**
     * Get the time between the start and end time
     * @return [double] The time difference between `startTime` and `endTime`
     */
    public double getTime() {
        return round(((double) (this.endTime - this.startTime)) / 1_000_000_000.0, 4);
    }

}