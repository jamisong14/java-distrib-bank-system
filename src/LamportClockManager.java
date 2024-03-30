package src;

public class LamportClockManager {
    
    private int clock;

    public LamportClockManager() {
        this.clock = 0;
    }

    public synchronized void increment() {
        this.clock++;
    }

    public synchronized int getClockValue() {
        return this.clock;
    }
}
