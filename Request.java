import java.io.Serializable;

public class Request implements Serializable {

    public static enum Type {
        CREATE_ACCOUNT,
        GET_BALANCE,
        DEPOSIT,
        TRANSFER
    }

    private String timestamp;
    private Type type;
    private int uid;
    private int amount;
    private int fromUid;
    private int toUid;

    public Request withTimestamp(String timestamp) {
        this.timestamp = timestamp;
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

    public String getTimestamp() {
        return timestamp;
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
}
