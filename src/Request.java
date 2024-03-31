/**
 * @file src/Request.java
 * @brief Request class used for sending requests between clients and servers (and server-server communication).
 *          This class has been created using the builder pattern to easily add parameters to the request without
 *          having to create a constructor with many parameters or many subclasses of the Request class.
 * @created 2024-03-30
 * @author Jamison Grudem (grude013)
 * 
 * @grace_days Using 2 grace days
 */

package src;

import java.io.Serializable;

public class Request implements Serializable {

    // Supported operations
    public static enum Type {
        CREATE_ACCOUNT,
        GET_BALANCE,
        DEPOSIT,
        TRANSFER,
        HALT
    }

    // Stores the lamport clock
    private LamportClock clock;
    // Stores the type of request
    private Type type;
    // Store the uid for the account
    private int uid;
    // Stores the amount for a deposit/transfer
    private int amount;
    // Stores the uid of the account to transfer from
    private int fromUid;
    // Stores the uid of the account to transfer to
    private int toUid;
    // Stores the origin of the request, used for loggin
    private String origin;

    /**
     * Add a lamport clock to the request
     */
    public Request withClock(LamportClock clock) {
        this.clock = clock;
        return this;
    }

    /**
     * Set the type of the request
     */
    public Request ofType(Type type) {
        this.type = type;
        return this;
    }

    /**
     * Set the uid of the account
     */
    public Request withUid(int uid) {
        this.uid = uid;
        return this;
    }

    /**
     * Set the amount for a deposit/transfer
     */
    public Request withAmount(int amount) {
        this.amount = amount;
        return this;
    }
    
    /**
     * Set the uid of the account to transfer from
     */
    public Request from(int fromUid) {
        this.fromUid = fromUid;
        return this;
    }

    /**
     * Set the uid of the account to transfer to
     */
    public Request to(int toUid) {
        this.toUid = toUid;
        return this;
    }

    /**
     * Set the origin of the request
     */
    public Request withOrigin(String origin) {
        this.origin = origin;
        return this;
    }

    /**
     * Get the lamport clock
     * @return [LamportClock] The lamport clock
     */
    public LamportClock getClock() {
        return clock;
    }

    /**
     * Get the type of the request
     * @return [Type] The type of the request
     */
    public Type getType() {
        return type;
    }

    /**
     * Get the uid of the account
     * @return [int] The uid of the account
     */
    public int getUid() {
        return uid;
    }

    /**
     * Get the amount for a deposit/transfer
     * @return [int] The amount
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Get the uid of the account to transfer from
     * @return [int] The uid of the account
     */
    public int getFrom() {
        return fromUid;
    }

    /**
     * Get the uid of the account to transfer to
     * @return [int] The uid of the account
     */
    public int getTo() {
        return toUid;
    }

    /**
     * Get the origin of the request
     * @return [String] The origin of the request
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * Convert the request's parameters to a string, used for logging
     * @return [String] The request's parameters as a string
     */
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
