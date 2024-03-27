/**
 * Authors:
 *  - Jamison Grudem(grude013)
 *  - Manan Mrig (mrig0001)
 */

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IBankServer extends Remote {
    public int createAccount(int uid) throws RemoteException;
    public int getBalance(int uid) throws RemoteException;
    public boolean deposit(int uid, int amount) throws RemoteException;
    public boolean transfer(int fromUid, int toUid, int amount) throws RemoteException;
    public void multicast(Request req) throws RemoteException;
    public void clientRequest(Request req, String origin) throws RemoteException;
    public void serverRequest(Request req, String origin) throws RemoteException;
    public Response execute() throws RemoteException;
} 