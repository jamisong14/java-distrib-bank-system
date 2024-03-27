/**
 * Authors:
 *  - Jamison Grudem(grude013)
 *  - Manan Mrig (mrig0001)
 */

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.net.InetAddress;
import java.io.File;
import java.time.LocalDateTime;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


public class BankServer implements IBankServer {

    private int serverId = -1;
    private int rmiPort = 1099;
    ConcurrentHashMap<Integer, Account> accounts = new ConcurrentHashMap<Integer, Account>();
    Document configDoc;
    ArrayList<Request> requestQueue = new ArrayList<Request>();
    
    /**
     * Default constructor for BankServer
     * @throws RemoteException
     */
    public BankServer() throws RemoteException {
        super();
    }
    
    /**
     * Constructor for BankServer
     * @param rmiPort The port to bind the RMI server to
     * @param configDoc The configuration XML file
     */
    public BankServer(int serverId, int rmiPort, Document configDoc) throws RemoteException {
        super();
        this.serverId = serverId;
        this.rmiPort = rmiPort;
        this.configDoc = configDoc;
    }

    /**
     * Create a new account
     */
    public synchronized int createAccount(int uid) throws RemoteException {
        Account a = new Account(uid);
        accounts.put(a.getUid(), a);
        return a.getUid();
    }

    /**
     * Get the balance of an account
     * @param uid The account id
     */
    public synchronized int getBalance(int uid) throws RemoteException {
        if (!accounts.containsKey(uid)) {
            throw new RemoteException("[Balance] Account not found");
        }
        System.out.println("[Account " + uid + "] Balance: " + accounts.get(uid).getBalance());
        return accounts.get(uid).getBalance();
    }

    /**
     * Deposit money into an account
     * @param uid The account id
     * @param amount The amount to deposit
     */
    public synchronized boolean deposit(int uid, int amount) throws RemoteException {
        // Check to make sure account with uid exists
        if (!accounts.containsKey(uid)) {
            throw new RemoteException("[Deposit] Account not found");
        }
        Account a = accounts.get(uid);
        a.setBalance(amount + a.getBalance());
        return true;
    }

    /**
     * Transfer money from one account to another
     * @param fromUid The account id to transfer from
     * @param toUid The account id to transfer to
     * @param amount The amount to transfer
     */
    public synchronized boolean transfer(int fromUid, int toUid, int amount) throws RemoteException {
        // Check to make sure account with uid exists
        if (!accounts.containsKey(fromUid) || !accounts.containsKey(toUid)) {
            throw new RemoteException("[Transfer] Account(s) not found");
        }
        // Get to and from accounts
        Account from = accounts.get(fromUid);
        Account to = accounts.get(toUid);
        // Verify that from account has enough money
        if (from.getBalance() < amount) { 
            return false;
        }
        // Transfer money
        from.setBalance(from.getBalance() - amount);
        to.setBalance(to.getBalance() + amount);
        return true;
    }

    /**
     * Multicast a request to all servers - used internally
     * @param req The request to multicast
     */
    public synchronized void multicast(Request req) throws RemoteException {
        System.out.println("[" + LocalDateTime.now() + "]: Multicasting request: " + req.getType());
        int serverCount = configDoc.getElementsByTagName("server").getLength();
        NodeList hostnames = configDoc.getElementsByTagName("hostname");
        NodeList ports = configDoc.getElementsByTagName("port");

        for(int i = 0; i < serverCount; i++) {
            // No need to send request to self
            if(i != serverId) {
                System.out.println("[" + LocalDateTime.now() + "]: Sending request to server " + i);
                try {
                    String url = new String("//" + hostnames.item(i).getTextContent() + ":" + ports.item(i).getTextContent() + "/BankServer");
                    IBankServer server = (IBankServer) Naming.lookup(url);
                    String origin = "Server-" + serverId;
                    server.receive(req, origin, false);
                } catch(Exception e) {
                    System.out.println("Error: " + e);
                }
            }
        }
    }

    /**
     * Receive a request from a client. Adds the request to the queue and multicasts it
     * @param req The request to receive
     */
    public synchronized Response receive(Request req, String origin, boolean fromClient) throws RemoteException {
        System.out.println("[" + LocalDateTime.now() + "]: Received a new request: " + req.getType());
        System.out.println("uid: " + req.getUid() + ", amount: " + req.getAmount() + ", from: " + req.getFrom() + ", to: " + req.getTo());
        requestQueue.add(req);

        // Only multicast if the request is not from another server
        if(fromClient) {
            multicast(req);
            Printer.print("Server-" + serverId + " | CLIENT-REQ | " + LocalDateTime.now() + " | [?, " + serverId + "] | " + origin + " | " + req.getType() + " |", Printer.File.SERVER, "" + serverId);
        }
        else 
            Printer.print("Server-" + serverId + " | SRV-REQ | " + LocalDateTime.now() + " | [?, " + serverId + "] | " + origin + " | " + req.getType() + " |", Printer.File.SERVER, "" + serverId);

        // TESTING: Execute the request immediately
        return execute();
        // return (new Response());
    }

    /**
     * Execute the next request in the queue
     */
    public synchronized Response execute() {
        if(requestQueue.size() > 0) {
            Request req = requestQueue.get(0);
            requestQueue.remove(0);

            try {
                switch(req.getType()) {
                    case CREATE_ACCOUNT:
                        return (new Response()).ofType(Response.Type.CREATE_ACCOUNT).withUid(createAccount(req.getUid()));
                    case GET_BALANCE:
                        return (new Response()).ofType(Response.Type.GET_BALANCE).withBalance(getBalance(req.getUid()));
                    case DEPOSIT:
                        return (new Response()).ofType(Response.Type.DEPOSIT).withSuccess(deposit(req.getUid(), req.getAmount()));
                    case TRANSFER:
                        return (new Response()).ofType(Response.Type.TRANSFER).withSuccess(transfer(req.getFrom(), req.getTo(), req.getAmount()));
                    default:
                        return (new Response());
                }
            }
            catch(RemoteException e) {
                System.out.println("RemoteException Error: " + e);
                return (new Response());
            }
        }
        return (new Response());
    }
    
    // RMI Server Shutdown Cleanup
    public void shutdown() {
        try {
            System.out.println("Shutting down server");
            Registry localRegistry = LocateRegistry.getRegistry(rmiPort);
            localRegistry.unbind("BankServer");
            System.out.println("Unbinding complete");
            UnicastRemoteObject.unexportObject(this, true);
            System.out.println("Unexport complete");
        } catch(Exception e) {
            System.out.println("Error: " + e);
        }
    }

    public static void main(String[] args) {

        // Initialize variables
        BankServer bankServer;
        IBankServer bankServerStub;
        Registry localRegistry;
        Document configDoc;
        int serverId = -1;
        int rmiPort = 1099;

        // Validate command line arguments
        if (args.length != 2) {
            System.out.println("Usage: java BankServer <serverId> <configFile>");
            return;
        }
        // Parse command line arguments
        else {
            try {
                serverId = Integer.parseInt(args[0]);
                String configFile = args[1];
    
                // Parse config file
                File file = new File(configFile);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                configDoc = db.parse(file);
    
                // Get the rmi port based off the server id
                rmiPort = Integer.parseInt(configDoc.getElementsByTagName("port").item(serverId).getTextContent());
                System.out.println("RMI Port: " + rmiPort);
            } 
            // Entered serverId is not a number
            catch (NumberFormatException nfe) {
                System.out.println("Please enter a valid serverId. It must be a number.");
                return;
            }
            // Error reading the configuration file
            catch (Exception e) {
                System.out.println("There was an error reading the configuration file: " + e);
                return;
            }
        }

        // Attempt to start the server
        try {
            bankServer = new BankServer(serverId, rmiPort, configDoc);
            System.setProperty("java.rmi.server.hostname", InetAddress.getLocalHost().getCanonicalHostName());
            bankServerStub = (IBankServer) UnicastRemoteObject.exportObject(bankServer, 0);
            localRegistry = LocateRegistry.createRegistry(rmiPort);

            String url = new String("//" + InetAddress.getLocalHost().getCanonicalHostName() + ":" + rmiPort + "/BankServer");
            System.out.println("URL: " + url);
            Naming.bind(url, bankServerStub);
            System.out.println("Completed binding to RMI registry on port " + rmiPort);

            // Create 20 new accounts and deposit 1000 into each
            for (int i = 1; i < 21; i++) {
                int uid = bankServer.createAccount(i);
                boolean res = bankServer.deposit(uid, 1000);
                System.out.println("Account created: " + uid + ", Deposit: " + res);
            }
    
            System.out.println("Server active on host: " + InetAddress.getLocalHost().getCanonicalHostName());
            Thread.sleep(300000);
    
            bankServer.shutdown();
            System.out.println("Server shutdown complete");
        }
        catch(Exception e) {
            System.out.println("Error: " + e);
            return;
        }
    }

}
