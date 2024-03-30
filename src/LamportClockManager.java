/**
 * @file src/LamportClockManager.java
 * @brief Simple class used to manage the current lamport clock value for a server.
 * @created 2024-03-30
 * @author Jamison Grudem (grude013)
 * 
 * @grace_days Using 1 grace day
 */

package src;

public class LamportClockManager {
    
    // Integer clock value
    private int clock;

    /**
     * Initialize a new LamportClockManager
     */
    public LamportClockManager() {
        this.clock = 0;
    }

    /**
     * Increment the clock value
     */
    public synchronized void increment() {
        this.clock++;
    }

    /**
     * Get the current clock value
     * @return [int] The clock value
     */
    public synchronized int getClockValue() {
        return this.clock;
    }
}
