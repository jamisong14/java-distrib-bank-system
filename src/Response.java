/**
 * @file src/Response.java
 * @brief Response class used for replying to requests between clients and servers (and server-server communication).
 *          This class has been created using the builder pattern to easily add parameters to the response without
 *          having to create a constructor with many parameters or many subclasses of the Response class.
 * @created 2024-03-30
 * @author Jamison Grudem (grude013)
 * 
 * @grace_days Using 2 grace days
 */

package src;

import java.io.Serializable;

public class Response implements Serializable {

    // Supported operations
    public static enum Type {
        CREATE_ACCOUNT,
        GET_BALANCE,
        DEPOSIT,
        TRANSFER,
        ACK,
        NACK,
        HALT
    }

    // Stores the lamport clock
    private LamportClock clock;
    // Stores the type of response
    private Type type;
    // Store the uid for the account
    private int uid;
    // Stores the balance for the account
    private int balance;
    // Stores the success of the operation
    private boolean success;

    /**
     * Add a lamport clock to the response
     */
    public Response withClock(LamportClock clock) {
        this.clock = clock;
        return this;
    }

    /**
     * Set the type of the response
     */
    public Response ofType(Type type) {
        this.type = type;
        return this;
    }

    /**
     * Set the uid of the account
     */
    public Response withUid(int uid) {
        this.uid = uid;
        return this;
    }

    /**
     * Set the balance of the account
     */
    public Response withBalance(int balance) {
        this.balance = balance;
        return this;
    }

    /**
     * Set the success of the operation
     */
    public Response withSuccess(boolean success) {
        this.success = success;
        return this;
    }

    /**
     * Return a string representation of the Response
     */
    public LamportClock getClock() {
        return clock;
    }

    /**
     * Return the type of the response
     */
    public Type getType() {
        return type;
    }

    /**
     * Return the uid of the account
     */
    public int getUid() {
        return uid;
    }

    /**
     * Return the balance of the account
     */
    public int getBalance() {
        return balance;
    }

    /**
     * Return the success of the operation
     */
    public boolean getSuccess() {
        return success;
    }
}
