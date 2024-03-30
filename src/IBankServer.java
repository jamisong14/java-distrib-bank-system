/**
 * Authors:
 *  - Jamison Grudem(grude013)
 */
package src;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IBankServer extends Remote {
    public int getId() throws RemoteException;
    public void printQueue(String from) throws RemoteException;
    public Response clientRequest(Request req) throws RemoteException;
    public Response serverRequest(Request req) throws RemoteException;
    public Response execute(Request req) throws RemoteException;
} 