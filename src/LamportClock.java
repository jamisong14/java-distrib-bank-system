/**
 * @file src/LamportClock.java
 * @brief Represents the lamport clock timestamp associated with requests.
 * @created 2024-03-30
 * @author Jamison Grudem (grude013)
 * 
 * @grace_days Using 2 grace days
 */

package src;

import java.io.Serializable;

public class LamportClock implements Serializable {
    // Stores the timestamp
    private int timestamp;
    // Stores the server id
    private int serverId;

    /**
     * Initialize a new LamportClock
     * @param timestamp The timestamp
     * @param serverId The id of the server
     */
    public LamportClock(int timestamp, int serverId) {
        this.timestamp = timestamp;
        this.serverId = serverId;
    }

    /**
     * Return the timestamp
     */
    public int getTimestamp() {
        return this.timestamp;
    }

    /**
     * Return the server id
     */
    public int getServerId() {
        return this.serverId;
    }

    /**
     * Compare two lamport clocks:
     *  - Integer compare by timestamp first
     *  - Break ties with the server id
     * @param other The other LamportClock to compare to
     * @return -1 if this is less than other, 0 if equal, 1 if greater
     */
    public int compareTo(LamportClock other) {
        if(this.timestamp < other.getTimestamp())
            return -1;
        else if(this.timestamp > other.getTimestamp())
            return 1;
        else {
            if(this.serverId < other.getServerId())
                return -1;
            else if(this.serverId > other.getServerId())
                return 1;
            else
                return 0;
        }
    }

    /**
     * Return a string representation of the LamportClock
     */
    public String toString() {
        return "[" + this.timestamp + ", " + this.serverId + "]";
    }
}
