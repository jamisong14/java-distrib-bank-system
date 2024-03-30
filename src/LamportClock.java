package src;

import java.io.Serializable;

public class LamportClock implements Serializable {
    
    private int timestamp;
    private int serverId;

    public LamportClock(int timestamp, int serverId) {
        this.timestamp = timestamp;
        this.serverId = serverId;
    }

    public int getTimestamp() {
        return this.timestamp;
    }

    public int getServerId() {
        return this.serverId;
    }

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

    public String toString() {
        return "[" + this.timestamp + ", " + this.serverId + "]";
    }
}
