import java.io.Serializable;

public class Response implements Serializable {

    public static enum Type {
        CREATE_ACCOUNT,
        GET_BALANCE,
        DEPOSIT,
        TRANSFER
    }

    private String timestamp;
    private Type type;
    private int uid;
    private int balance;
    private boolean success;

    public Response withTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public Response ofType(Type type) {
        this.type = type;
        return this;
    }

    public Response withUid(int uid) {
        this.uid = uid;
        return this;
    }

    public Response withBalance(int balance) {
        this.balance = balance;
        return this;
    }

    public Response withSuccess(boolean success) {
        this.success = success;
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
    public int getBalance() {
        return balance;
    }
    public boolean getSuccess() {
        return success;
    }
}
