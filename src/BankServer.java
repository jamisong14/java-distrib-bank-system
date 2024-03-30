package src;
/**
 * Authors:
 *  - Jamison Grudem(grude013)
 */

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

    private int serverId = -1;
    private int rmiPort = 1099;
    ConcurrentHashMap<Integer, Account> accounts = new ConcurrentHashMap<Integer, Account>();
    ArrayList<Request> requestQueue = new ArrayList<Request>();
    ArrayList<Request> executedRequests = new ArrayList<Request>();
    IBankServer[] peerServers;
    LamportClockManager clockManager;
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

    public int getId() {
        return serverId;
    }

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
            throw new RemoteException("[Balance] Account " + uid + " not found");
        }
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

    public synchronized void halt() throws RemoteException {
        int total = 0;
        for(int i = 1; i < 21; i++) {
            total += getBalance(i);
            Printer.print("Server-" + serverId + " | | " + LocalDateTime.now() + " | | | GET_BALANCE | balance=" + getBalance(i), Printer.File.SERVER, "" + serverId, "#b2b7f7");
        }
        Printer.print("Server-" + serverId + " | | " + LocalDateTime.now() + " | | | TOTAL | balance=" + total, Printer.File.SERVER, "" + serverId, "#737bf0");

        Printer.print("Server-" + serverId + " | | " + LocalDateTime.now() + " | | | QUEUE_PRINT", Printer.File.SERVER, "" + serverId, "#b2b7f7");
        for(Request r : requestQueue) {
            Printer.print("Server-" + serverId + " | | " + LocalDateTime.now() + " | | | QUEUE | " + r.getType() + " | " + r.parametersToString(), Printer.File.SERVER, "" + serverId, "#b2b7f7");
        }
        Printer.print("Server-" + serverId + " | | " + LocalDateTime.now() + " | | | EXIT", Printer.File.SERVER, "" + serverId, "#737bf0");
        // this.shutdown();

        shutdown(this, serverId, rmiPort);
    }

    // RMI Server Shutdown Cleanup
    public void shutdown(BankServer server, int serverId, int rmiPort) {
        Thread t = new Thread(() -> shutdownStatic(server, serverId, rmiPort));
        t.start();
    }

    public static void shutdownStatic(BankServer server, int serverId, int rmiPort) {
        try {
            // Get the average request time
            double avgTime = 0;
            for(double time : server.timeEntries) {
                avgTime += time;
            }
            avgTime = avgTime / server.timeEntries.size();

            Printer.print("Server-" + serverId + " | | | | | REPORT | avg request time=" + avgTime + "s", Printer.File.SERVER, "" + serverId, "#737bf0");
            Printer.closeHtmlLog(Printer.File.SERVER, "" + serverId);
            System.out.println("Shutting down server...");
            Registry localRegistry = LocateRegistry.getRegistry(rmiPort);
            localRegistry.unbind("BankServer");
            UnicastRemoteObject.unexportObject(server, true);
            System.out.println("Server shutdown complete");
            System.exit(0);
        } catch(Exception e) {
            System.out.println("Error: " + e);
        }
    }

    public synchronized void addRequestInSequence(Request req) {
        requestQueue.add(req);
        requestQueue.sort((r1, r2) -> r1.getClock().compareTo(r2.getClock()));
    }

    public synchronized void addTime(double time) {
        timeEntries.add(time);
    }

    public synchronized void printQueue(String from) {
        // Print out the request queue
        // String queue = "";
        // for(Request r : requestQueue) {
        //     queue += "\t" + r.getOrigin() + " | " + r.getType() + " | " + r.parametersToString() + " | " + r.getClock() + "\n";
        // }
        // Printer.print(from + " Request Queue (size: " + requestQueue.size() + ")\n" + queue, Printer.File.SERVER, "" + serverId);
    }

    /**
     * Multicast a request to all servers - used internally
     * @param req The request to multicast
     */
    public Response[] multicast(Request req) throws RemoteException {
        req = req.withOrigin("Server-" + serverId);
        Response[] responses = new Response[peerServers.length];
        for(int i = 0; i < peerServers.length; i++) {
            IBankServer peer = peerServers[i];
            Printer.print("Server-" + serverId + " | -> SRV-REQ | " + LocalDateTime.now() + " | " + req.getClock() + " | Server-" + peer.getId() + " | " + req.getType() + " | " + req.parametersToString(), Printer.File.SERVER, "" + serverId, "#de9050");
            Response res = peer.serverRequest(req);
            Printer.print("Server-" + serverId + " | SRV-RES    | " + LocalDateTime.now() + " | " + req.getClock() + " | Server-" + peer.getId() + " | " + req.getType(), Printer.File.SERVER, "" + serverId, "#b2f7b9");
            responses[i] = res;
        }
        return responses;
    }

    public Response clientRequest(Request req) throws RemoteException {
        Timer timer = new Timer();
        timer.start();
        clockManager.increment();
        req = req.withClock(new LamportClock(clockManager.getClockValue(), serverId));
        Printer.print("Server-" + serverId + " | CLIENT-REQ | " + LocalDateTime.now() + " | " + req.getClock() + " | " + req.getOrigin() + " | " + req.getType() + " | " + req.parametersToString(), Printer.File.SERVER, "" + serverId, "#e67417");

        addRequestInSequence(req);

        // No need to multicast a get balance request - we are not modifying anything so just execute this right away
        Response[] responses = new Response[peerServers.length];
        Request.Type reqType = req.getType();
        if(reqType != Request.Type.GET_BALANCE)
            responses = this.multicast(req);
        else
            return this.execute(req);

        printQueue("From ClientRequest " + req.getClock());

        // Wait for the current request to be at the head of the queue
        while(requestQueue.get(0) != req) {
            printQueue("| " + LocalDateTime.now() + " | Waiting for head request " + req.getClock() + " | Current head: " + requestQueue.get(0).getClock());
            // System.out.println("Waiting for request to be at the head of the queue " + req.getClock());
        }

        // Printer.print("Executing request locally: " + req.getClock() + " | " + peerServers.length + " peers to execute on", Printer.File.SERVER, "" + serverId);
        for(IBankServer peer : peerServers) {
            // System.out.println("Executing request on peer: " + peer.getId() + " | " + req.getClock());
            peer.execute(req);
        }
        timer.stop();
        addTime(timer.getTime());
        timer.clear();
        
        return this.execute(req);
    }

    public Response serverRequest(Request req) throws RemoteException {
        Printer.print("Server-" + serverId + " | <- SRV-REQ | " + LocalDateTime.now() + " | " + req.getClock() + " | " + req.getOrigin() + " | " + req.getType() + " | " + req.parametersToString(), Printer.File.SERVER, "" + serverId, "#e3b28a");
        addRequestInSequence(req);

        printQueue("From ServerRequest" + req.getClock());
        return (new Response()).ofType(Response.Type.ACK).withClock(req.getClock());
    }

    /**
     * Execute the next request in the queue
     */
    public synchronized Response execute(Request req) {
        Printer.print("Server-" + serverId + " | EXECUTE   | " + LocalDateTime.now() + " | " + req.getClock() + " | " + req.getOrigin() + " | " + req.getType() + " | " + req.parametersToString(), Printer.File.SERVER, "" + serverId, "#5fe8e6");

        LamportClock reqClock = req.getClock();
        // Find the request in the queue and remove it
        for(Request r : requestQueue) {
            if(r.getClock().compareTo(reqClock) == 0) {
                requestQueue.remove(r);
                printQueue("| " + LocalDateTime.now() + " | Removed request from queue " + req.getClock());
                break;
            }
        }

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

    public static void main(String[] args) {

        // Initialize variables
        BankServer bankServer;
        IBankServer bankServerStub;
        Registry localRegistry;
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
            // Start this server
            Printer.print("Server-" + serverId + " |    LIVE    | " + LocalDateTime.now(), Printer.File.SERVER, "" + serverId, "#737bf0");
            bankServer = new BankServer(serverId, rmiPort, serverCount - 1);
            System.setProperty("java.rmi.server.hostname", hostname);
            bankServerStub = (IBankServer) UnicastRemoteObject.exportObject(bankServer, 0);
            localRegistry = LocateRegistry.createRegistry(rmiPort);
            String url = new String("//" + hostname + ":" + rmiPort + "/BankServer");
            Naming.bind(url, bankServerStub);
            System.out.println("Server started on //" + hostname + ":" + rmiPort);

            // Find the peer servers
            for (int i = 0; i < serverCount; i++) {
                if (i == serverId) {
                    continue;
                }
                String host = configDoc.getElementsByTagName("hostname").item(i).getTextContent();
                int port = Integer.parseInt(configDoc.getElementsByTagName("port").item(i).getTextContent());
                int c = 1;
                System.out.println("Waiting for Server-" + i + " @ //" + hostname + ":" + port + " to start...");
                while(true) {
                    Printer.print("Server-" + serverId + " |  PEER-CON  | " + LocalDateTime.now() + " | | | ATTEMPT " + c + " | " + host + ":" + port, Printer.File.SERVER, "" + serverId, "#b2b7f7");
                    try {
                        IBankServer peer = (IBankServer) Naming.lookup("//" + host + ":" + port + "/BankServer");
                        Printer.print("Server-" + serverId + " |  PEER-CON  | " + LocalDateTime.now() + " | | | SUCCESS | " + host + ":" + port, Printer.File.SERVER, "" + serverId, "#b2b7f7");
                        bankServer.addPeer(peer);
                        System.out.println("Peer Server-" + i + " connected");
                        break;
                    }
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
                // System.out.println("Account created: " + uid + ", Deposit: " + res);
            }
            System.out.println("Initialization complete, ready for requests.");
            Printer.print("Server-" + serverId + " |    INIT    | " + LocalDateTime.now(), Printer.File.SERVER, "" + serverId, "#737bf0");

            final int sid = serverId;
            Thread shutdownThread = new Thread() {
                @Override
                public void run() {
                    try {
                        Printer.closeHtmlLog(Printer.File.SERVER, "" + sid);
                    }
                    catch (Exception e) {
                        System.err.println("Couldn't close log file before terminating.");
                        e.printStackTrace();
                    }
                }
            };
            Runtime.getRuntime().addShutdownHook(shutdownThread);

            Thread.sleep(300000);
    
            bankServer.shutdown(bankServer, serverId, rmiPort);
            System.out.println("Server shutdown complete");
        }
        catch(Exception e) {
            System.out.println("Another Error: " + e);
            e.printStackTrace();
            return;
        }
    }

}
