/**
 * @file src/BankServer.java
 * @brief The BankServer class is the actual implementation of the IBankServer interface. It is responsible for performing bank operations
 *          such as creating accounts, getting balances, etc. The BankServer also handles server-server communication and synchronization 
 *          between copies of the database. The BankServer accepts a configuration file as a command line argument to determine the server's
 *          hostname, port, and the hostname and port of its peers. The BankServer will listen for requests, mutlicasting them between its
 *          peers until a HALT command is received. The BankServer will then print out the final balances of all accounts and exit.
 * @created 2024-03-30
 * @author Jamison Grudem (grude013)
 * 
 * @grace_days Using 1 grace day
 */

package src;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.io.File;
import java.time.LocalDateTime;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

public class BankServer implements IBankServer {

    // Store the server id
    private int serverId = -1;
    // Store the rmi port the server is running on
    private int rmiPort = 1099;
    // Our local copy of the database - hash map of accounts
    ConcurrentHashMap<Integer, Account> accounts = new ConcurrentHashMap<Integer, Account>();
    // Queue of requests to be executed
    ArrayList<Request> requestQueue = new ArrayList<Request>();
    // Array of peer servers
    IBankServer[] peerServers;
    // The clock manager for the server - stores timestamps
    LamportClockManager clockManager;
    // Store the time it takes to execute requests
    ArrayList<Double> timeEntries = new ArrayList<Double>();
    
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
    public BankServer(int serverId, int rmiPort, int peerCount) throws RemoteException {
        super();
        this.serverId = serverId;
        this.rmiPort = rmiPort;
        this.peerServers = new IBankServer[peerCount];
        this.clockManager = new LamportClockManager();
    }

    /**
     * Add a peer server to the list of servers
     * @param peer IBankServer object to add
     */
    public void addPeer(IBankServer peer) {
        for(int i = 0; i < peerServers.length; i++) {
            if(peerServers[i] == null) {
                peerServers[i] = peer;
                return;
            }
        }
    }

    /**
     * Create a new account
     * @param uid The account id
     * @return The account id
     */
    public synchronized int createAccount(int uid) throws RemoteException {
        Account a = new Account(uid);
        accounts.put(a.getUid(), a);
        return a.getUid();
    }

    /**
     * Get the balance of an account
     * @param uid The account id
     * @return The balance of the account
     */
    public synchronized int getBalance(int uid) throws RemoteException {
        if (!accounts.containsKey(uid)) {
            throw new RemoteException("[Balance] Account " + uid + " not found");
        }
        return accounts.get(uid).getBalance();
    }

    /**
     * Deposit money into an account
     * @param uid The account id
     * @param amount The amount to deposit
     * @return True if the deposit was successful, false otherwise
     */
    public synchronized boolean deposit(int uid, int amount) throws RemoteException {
        // Check to make sure account with uid exists
        if (!accounts.containsKey(uid)) {
            throw new RemoteException("[Deposit] Account " + uid + " not found");
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
     * @return True if the transfer was successful, false otherwise
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
     * Halt the server, print out the final balances of all accounts and the request queue, then shutdown
     * @throws RemoteException
     */
    public synchronized void halt() throws RemoteException {
        // Log balances of all accounts
        int total = 0;
        for(int i = 1; i < 21; i++) {
            total += getBalance(i);
            Printer.print("Server-" + serverId + " | | " + LocalDateTime.now() + " | | | GET_BALANCE | balance=" + getBalance(i), Printer.File.SERVER, "" + serverId, "#b2b7f7");
        }
        Printer.print("Server-" + serverId + " | | " + LocalDateTime.now() + " | | | TOTAL | balance=" + total, Printer.File.SERVER, "" + serverId, "#737bf0");

        // Log the request queue
        Printer.print("Server-" + serverId + " | | " + LocalDateTime.now() + " | | | QUEUE_PRINT", Printer.File.SERVER, "" + serverId, "#b2b7f7");
        for(Request r : requestQueue) {
            Printer.print("Server-" + serverId + " | | " + LocalDateTime.now() + " | | | QUEUE | " + r.getType() + " | " + r.parametersToString(), Printer.File.SERVER, "" + serverId, "#b2b7f7");
        }
        Printer.print("Server-" + serverId + " | | " + LocalDateTime.now() + " | | | EXIT", Printer.File.SERVER, "" + serverId, "#737bf0");

        // Shutdown the server
        shutdown(this, serverId, rmiPort);
    }

    /**
     * Shutdown the server in a new thread.
     * 
     * We do this because when the server multicasts a HALT message, it waits for a response
     * but it will never get one if the server shuts down before sending a response. The server
     * will send its response and then shutdown in its own thread.
     * @param server The BankServer object
     * @param serverId The server id
     * @param rmiPort The RMI port
     */
    public void shutdown(BankServer server, int serverId, int rmiPort) {
        Thread t = new Thread(() -> shutdownStatic(server, serverId, rmiPort));
        t.start();
    }

    /**
     * Static method of the traditional shutdown() method provided in Canvas code samples.
     * This method was created as threads cannot call instance methods. We need to create 
     * a static solution with the provided instance to shutdown.
     * @param server The BankServer object
     * @param serverId The server id
     * @param rmiPort The RMI port
     */
    public static void shutdownStatic(BankServer server, int serverId, int rmiPort) {
        try {
            // Get and log the total average request time
            double avgTime = 0;
            for(double time : server.timeEntries) {
                avgTime += time;
            }
            avgTime = avgTime / server.timeEntries.size();
            Printer.print("Server-" + serverId + " | | | | | REPORT | avg request time=" + avgTime + "s", Printer.File.SERVER, "" + serverId, "#737bf0");

            // Close logs, unbind the server, and unexport the object
            Printer.closeHtmlLog(Printer.File.SERVER, "" + serverId);
            System.out.println("Shutting down server...");
            Registry localRegistry = LocateRegistry.getRegistry(rmiPort);
            localRegistry.unbind("BankServer");
            UnicastRemoteObject.unexportObject(server, true);

            // Exit the program
            System.out.println("Server shutdown complete");
            System.exit(0);
        } catch(Exception e) {
            System.out.println("Error: " + e);
        }
    }

    /**
     * Add a new request object into the request queue in sorted order based on Lamport Clocks
     * @param req The request object to add
     */
    public synchronized void addRequestInSequence(Request req) {
        requestQueue.add(req);
        requestQueue.sort((r1, r2) -> r1.getClock().compareTo(r2.getClock()));
    }

    /**
     * Add a new time entry to the timeEntries list
     * @param time The time to add (in seconds)
     */
    public synchronized void addTime(double time) {
        timeEntries.add(time);
    }

    /**
     * [IBankServer] RMI INTERFACE
     * 
     * Get the server id
     */
    public int getId() {
        return serverId;
    }

    /**
     * Multicast a request to all servers - used internally
     * @param req The request to multicast
     */
    public Response[] multicast(Request req) throws RemoteException {
        // Build the current server as the origin of the request, rather than the client
        req = req.withOrigin("Server-" + serverId);

        // Multicast the request to all peer servers
        Response[] responses = new Response[peerServers.length];
        for(int i = 0; i < peerServers.length; i++) {
            IBankServer peer = peerServers[i];
            // Log request
            Printer.print("Server-" + serverId + " | -> SRV-REQ | " + LocalDateTime.now() + " | " + req.getClock() + " | Server-" + peer.getId() + " | " + req.getType() + " | " + req.parametersToString(), Printer.File.SERVER, "" + serverId, "#de9050");
            // Send request
            Response res = peer.serverRequest(req);
            // Log response
            Printer.print("Server-" + serverId + " | SRV-RES    | " + LocalDateTime.now() + " | " + req.getClock() + " | Server-" + peer.getId() + " | " + req.getType(), Printer.File.SERVER, "" + serverId, "#b2f7b9");
            responses[i] = res;
        }

        return responses;
    }

    /**
     * [IBankServer] RMI INTERFACE
     * 
     * Accept a new request from a client. 
     *  - Multicast the request to all servers
     *  - Wait for all servers to respond with ACK
     *  - Wait for the current request to be at the head of the queue
     *  - Send execute message to all peers
     *  - Execute the request locally and return as reponse
     * 
     * @param req The request object
     * @return The response from executing the request
     */
    public Response clientRequest(Request req) throws RemoteException {
        // Create and start the timer
        Timer timer = new Timer();
        timer.start();

        // Increment the clock and update the request's clock
        clockManager.increment();
        req = req.withClock(new LamportClock(clockManager.getClockValue(), serverId));
        Printer.print("Server-" + serverId + " | CLIENT-REQ | " + LocalDateTime.now() + " | " + req.getClock() + " | " + req.getOrigin() + " | " + req.getType() + " | " + req.parametersToString(), Printer.File.SERVER, "" + serverId, "#e67417");

        this.addRequestInSequence(req);

        // Mutlicast the request to all servers
        // Response[] responses = new Response[peerServers.length];
        Request.Type reqType = req.getType();
        if(reqType != Request.Type.GET_BALANCE)
            this.multicast(req);
        // No need to multicast a get balance request - we are not modifying anything so just execute this right away
        else
            return this.execute(req);


        // Wait for the current request to be at the head of the queue
        while(requestQueue.get(0) != req) {
            // Gives helpful figure of when client will finish
            System.out.println("Waiting for request: " + req.getClock() + ", Current head: " + requestQueue.get(0).getClock());
        }

        // Send execute message to all peers
        for(IBankServer peer : peerServers) {
            peer.execute(req);
        }

        // Stop time, request has finished
        timer.stop();
        addTime(timer.getTime());
        timer.clear();
        
        // Execute the request locally
        return this.execute(req);
    }

    /**
     * [IBankServer] RMI INTERFACE
     * 
     * Accept a new request from another server. Used for P2P multicasting.
     *  - Add the request to the queue
     *  - Return an ACK response
     * 
     * @param req The request object
     * @return An ACK response
     */
    public Response serverRequest(Request req) throws RemoteException {
        Printer.print("Server-" + serverId + " | <- SRV-REQ | " + LocalDateTime.now() + " | " + req.getClock() + " | " + req.getOrigin() + " | " + req.getType() + " | " + req.parametersToString(), Printer.File.SERVER, "" + serverId, "#e3b28a");
        addRequestInSequence(req);
        return (new Response()).ofType(Response.Type.ACK).withClock(req.getClock());
    }

    /**
     * [IBankServer] RMI INTERFACE
     * 
     * Execute a request locally
     *  - Find the request in the queue and remove it
     *  - Execute the request based on its type
     * 
     * @param req The request object
     * @return The response from executing the request
     */
    public synchronized Response execute(Request req) {
        // Log execution of request
        Printer.print("Server-" + serverId + " | EXECUTE   | " + LocalDateTime.now() + " | " + req.getClock() + " | " + req.getOrigin() + " | " + req.getType() + " | " + req.parametersToString(), Printer.File.SERVER, "" + serverId, "#5fe8e6");

        LamportClock reqClock = req.getClock();
        // Find the request in the queue by its clock and remove it
        for(Request r : requestQueue) {
            if(r.getClock().compareTo(reqClock) == 0) {
                requestQueue.remove(r);
                break;
            }
        }

        // Execute the corresponding methods based on the request type
        try {
            switch(req.getType()) {
                case CREATE_ACCOUNT:
                    return (new Response()).ofType(Response.Type.CREATE_ACCOUNT).withUid(createAccount(req.getUid())).withClock(req.getClock());
                case GET_BALANCE:
                    return (new Response()).ofType(Response.Type.GET_BALANCE).withBalance(getBalance(req.getUid())).withClock(req.getClock());
                case DEPOSIT:
                    return (new Response()).ofType(Response.Type.DEPOSIT).withSuccess(deposit(req.getUid(), req.getAmount())).withClock(req.getClock());
                case TRANSFER:
                    return (new Response()).ofType(Response.Type.TRANSFER).withSuccess(transfer(req.getFrom(), req.getTo(), req.getAmount())).withClock(req.getClock());
                case HALT:
                    halt();
                    return (new Response()).ofType(Response.Type.HALT).withClock(req.getClock());
                default:
                    return (new Response()).withClock(req.getClock());
            }
        }
        catch(RemoteException e) {
            System.out.println("RemoteException Error: " + e);
            return (new Response());
        }
    }

    /**
     * Main method for the BankServer
     * @param args Command line arguments
     */
    public static void main(String[] args) {

        // Initialize variables
        BankServer bankServer;
        IBankServer bankServerStub;
        Registry localRegistry; // Ignore this warning, necessary for binding
        Document configDoc;
        String hostname = "";
        int serverCount = 0;
        int serverId = -1;
        int rmiPort = 1099;

        // Validate command line arguments
        if (args.length != 2) {
            System.out.println("Usage: java BankServer <serverId> <configFile>");
            return;
        }
        // Parse command line arguments and configuration file
        else {
            try {
                serverId = Integer.parseInt(args[0]);
                String configFile = args[1];
    
                // Parse config file
                File file = new File(configFile);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                configDoc = db.parse(file);
                serverCount = configDoc.getElementsByTagName("server").getLength();
    
                // Get the rmi port based off the server id
                rmiPort = Integer.parseInt(configDoc.getElementsByTagName("port").item(serverId).getTextContent());
                hostname = configDoc.getElementsByTagName("hostname").item(serverId).getTextContent();
                Printer.initHtmlLog(Printer.File.SERVER, "" + serverId, "");
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
            // Create a bank server and bind it to RMI based off of configuration file
            bankServer = new BankServer(serverId, rmiPort, serverCount - 1);
            System.setProperty("java.rmi.server.hostname", hostname);
            bankServerStub = (IBankServer) UnicastRemoteObject.exportObject(bankServer, 0);
            localRegistry = LocateRegistry.createRegistry(rmiPort);
            String url = new String("//" + hostname + ":" + rmiPort + "/BankServer");
            Naming.bind(url, bankServerStub);

            // Log the server start
            Printer.print("Server-" + serverId + " |    LIVE    | " + LocalDateTime.now(), Printer.File.SERVER, "" + serverId, "#737bf0");
            System.out.println("Server started on //" + hostname + ":" + rmiPort);

            // Find the peer servers
            for (int i = 0; i < serverCount; i++) {
                // Ignore self
                if (i == serverId) {
                    continue;
                }

                // Get peer host and port
                String host = configDoc.getElementsByTagName("hostname").item(i).getTextContent();
                int port = Integer.parseInt(configDoc.getElementsByTagName("port").item(i).getTextContent());

                int c = 1; // Tracks number of attempts for peer connection
                System.out.println("Waiting for Server-" + i + " @ //" + host + ":" + port + " to start...");

                // Continuously attempt to connect to the peer server, waiting 1 second
                // between attempts until successful. The server will automatically detect when 
                // a peer server is started and connect to it.
                while(true) {
                    Printer.print("Server-" + serverId + " |  PEER-CON  | " + LocalDateTime.now() + " | | | ATTEMPT " + c + " | " + host + ":" + port, Printer.File.SERVER, "" + serverId, "#b2b7f7");
                    // Succesful connection
                    try {
                        IBankServer peer = (IBankServer) Naming.lookup("//" + host + ":" + port + "/BankServer");
                        Printer.print("Server-" + serverId + " |  PEER-CON  | " + LocalDateTime.now() + " | | | SUCCESS | " + host + ":" + port, Printer.File.SERVER, "" + serverId, "#737bf0");
                        bankServer.addPeer(peer);
                        System.out.println("Peer Server-" + i + " connected");
                        break;
                    }
                    // Error connecting, try again
                    catch(Exception e) {
                        c++;
                        Thread.sleep(1000);
                        continue;
                    }
                }
            }

            // Create 20 new accounts and deposit 1000 into each
            for (int i = 1; i < 21; i++) {
                int uid = bankServer.createAccount(i);
                boolean res = bankServer.deposit(uid, 1000);
                // Error handling
                if(!res) {
                    System.out.println("Error creating account or depositing money, uid=" + uid);
                    return;
                }
            }
            // Log the initialization of the server
            System.out.println("Initialization complete, ready for requests.");
            Printer.print("Server-" + serverId + " |    INIT    | " + LocalDateTime.now(), Printer.File.SERVER, "" + serverId, "#737bf0");

            // The following code snippet is necessary for properly closing the  
            // log file in the case of a server interruption (e.g. CTRL+C)  
            final int sid = serverId;
            Thread shutdownThread = new Thread() {
                @Override
                public void run() {
                    try { Printer.closeHtmlLog(Printer.File.SERVER, "" + sid); }
                    catch (Exception e) {
                        System.err.println("Couldn't close log file before terminating.");
                        e.printStackTrace();
                    }
                }
            };
            Runtime.getRuntime().addShutdownHook(shutdownThread);

            // Wait for 5 minutes before shutting down if no HALT request is received
            Thread.sleep(300000);
    
            // Shutdown the server
            bankServer.shutdown(bankServer, serverId, rmiPort);
            System.out.println("Server shutdown complete");
        }
        catch(Exception e) {
            System.out.println("Server Error: " + e);
            e.printStackTrace();
            return;
        }
    }

}
