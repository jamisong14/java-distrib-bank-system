package src;

import java.io.Serializable;

public class Request implements Serializable {

    public static enum Type {
        CREATE_ACCOUNT,
        GET_BALANCE,
        DEPOSIT,
        TRANSFER
    }

    private LamportClock clock;
    private Type type;
    private int uid;
    private int amount;
    private int fromUid;
    private int toUid;
    private String origin;

    public Request withClock(LamportClock clock) {
        this.clock = clock;
        return this;
    }

    public Request ofType(Type type) {
        this.type = type;
        return this;
    }

    public Request withUid(int uid) {
        this.uid = uid;
        return this;
    }

    public Request withAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public Request from(int fromUid) {
        this.fromUid = fromUid;
        return this;
    }

    public Request to(int toUid) {
        this.toUid = toUid;
        return this;
    }

    public Request withOrigin(String origin) {
        this.origin = origin;
        return this;
    }

    public LamportClock getClock() {
        return clock;
    }
    public Type getType() {
        return type;
    }
    public int getUid() {
        return uid;
    }
    public int getAmount() {
        return amount;
    }
    public int getFrom() {
        return fromUid;
    }
    public int getTo() {
        return toUid;
    }
    public String getOrigin() {
        return origin;
    }

    public String parametersToString() {
        switch(type) {
            case CREATE_ACCOUNT:
                return "uid=" + uid;
            case GET_BALANCE:
                return "uid=" + uid;
            case DEPOSIT:
                return "uid=" + uid + ", amount=" + amount;
            case TRANSFER:
                return "from=" + fromUid + ", to=" + toUid + ", amount=" + amount;
            default:
                return "";
        }
    }
}
