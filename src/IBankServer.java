/**
 * @file src/IBankServer.java
 * @brief Interface face for the RMI object BankServer. The methods below are RMI facing methods.
 * @created 2024-03-30
 * @author Jamison Grudem (grude013)
 * 
 * @grace_days Using 1 grace day
 */

package src;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IBankServer extends Remote {
    // Return the id of the server
    public int getId() throws RemoteException;
    // Accept a request from a client
    public Response clientRequest(Request req) throws RemoteException;
    // Accept a request from another server - used in P2P mutlicast
    public Response serverRequest(Request req) throws RemoteException;
    // Execute a request locally
    public Response execute(Request req) throws RemoteException;
} 