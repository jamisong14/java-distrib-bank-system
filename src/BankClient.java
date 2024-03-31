/**
 * @file src/BankClient.java
 * @brief The BankClient class is the client that will connect to the the BankServer(s) to perform random transfers between accounts.
 *          The client accepts a configuration file as an argument that specifies the hostname and port of each server. The client
 *          will initally establish connections between all servers and then create a number of client threads that will perform
 *          random transfers between accounts. The client will also verify the balance of each account after all the transfers have completed.
 * @created 2024-03-30
 * @author Jamison Grudem (grude013)
 * 
 * @grace_days Using 2 grace days
 */
package src;

import java.io.File;
import java.rmi.Naming;
import java.time.LocalDateTime;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

/**
 * ClientThread
 * Implements the threading class to perform multi-threaded random transfers to the server.
 */
class ClientThread extends Thread {

    // List of available servers
    private IBankServer[] servers;

    /**
     * Constructor
     * @param servers List of available servers
     */
    public ClientThread(IBankServer[] servers) {
        this.servers = servers;
    }

    /**
     * Run
     * Perform 200 random transfers between accounts, connecting to a random server each time.
     */
    public void run() {
        try {
            Timer timer = new Timer();

            // Perform 200 random transfers
            for(int i = 0; i < 200; i++) {
                // Choose a random server 
                int serverId = (int) (Math.random() * servers.length);
                IBankServer server = servers[serverId];

                // Transfer money between two random accounts
                int from = (int) (Math.random() * 20) + 1;
                int to = (int) (Math.random() * 20) + 1;
                while(to == from) {
                    to = (int) (Math.random() * 20) + 1;
                }

                // Build the request
                Request req = (new Request()).ofType(Request.Type.TRANSFER).from(from).to(to).withAmount(10).withOrigin("Thread-" + Thread.currentThread().getId());

                // Logging and timing
                Printer.print("T-" + Thread.currentThread().getId() + " | Server-" + serverId + " | REQ | " + LocalDateTime.now() + " | TRANSFER | from=" + from + ", to=" + to + ", amount=10", Printer.File.CLIENT, "", "#e3b28a");
                timer.start();

                // Send the request and get the response
                Response res = server.clientRequest(req);

                // Logging and timing
                timer.stop();
                Printer.print("T-" + Thread.currentThread().getId() + " | Server-" + serverId + " | RES | " + LocalDateTime.now() + " | " + res.getType() + " | time=" + timer.getTime() + "s, success=" + res.getSuccess() + ", timestamp=" + res.getClock(), Printer.File.CLIENT, "", "#b2f7b9");
                timer.clear();
            }

            // Log average transfer time for this thread
            Printer.print("T-" + Thread.currentThread().getId() + " | | | " + LocalDateTime.now() + " | REPORT | avg transfer time=" + timer.getAverage(), Printer.File.CLIENT, "", "#737bf0");
        } 
        // Catch any exceptions here
        catch (Exception e) {
            System.out.println("Client Thread Error: " + e);
            e.printStackTrace();
        }
    }
}

public class BankClient {

    public static void main(String args[]) throws Exception {

        // Validate command line arguments
        if (args.length != 2) {
            System.out.println("Usage: java BankClient <threadCount> <configFile>");
            return;
        }
        
        // Parse command line arguments
        int threadCount = Integer.parseInt(args[0]);
        String configFile = args[1];

        // Load the configuration file
        File file = new File(configFile);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document configDoc = db.parse(file);

        // Initialize the client logfile
        // Read from the config file to get the number of servers and their hostnames/ports
        // Create a header link to each server log
        int serverCount = configDoc.getElementsByTagName("server").getLength();
        String serverLinks = "";
        for (int i = 0; i < serverCount; i++) {
            String configSid = configDoc.getElementsByTagName("id").item(i).getTextContent();
            serverLinks += "<a href='server" + configSid + ".html'>Server-" + configSid + " Log</a> | ";
        }
        Printer.initHtmlLog(Printer.File.CLIENT, "", serverLinks);
        Printer.print("MAIN | | START | " + LocalDateTime.now(), Printer.File.CLIENT, "", "#b2b7f7");

        // Establish connections to all servers, storing their connections in an array
        IBankServer[] servers = new IBankServer[serverCount];
        for (int i = 0; i < serverCount; i++) {
            // Parse the hostname and port from the config file
            String host = configDoc.getElementsByTagName("hostname").item(i).getTextContent();
            int port = Integer.parseInt(configDoc.getElementsByTagName("port").item(i).getTextContent());
            // Connect to the server - log before and after
            Printer.print("MAIN | Server-" + i + " | REQ | " + LocalDateTime.now() + " | CONNECT | " + host + ":" + port, Printer.File.CLIENT, "", "#e3b28a");
            servers[i] = (IBankServer) Naming.lookup("//" + host + ":" + port + "/BankServer");
            Printer.print("MAIN | Server-" + i + " | RES | " + LocalDateTime.now() + " | | success=" + true, Printer.File.CLIENT, "", "#b2f7b9");
        }

        // Create and start the client threads
        System.out.println("Creating and starting client threads...");
        ClientThread[] threads = new ClientThread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new ClientThread(servers);
            threads[i].start();
        }
        
        // Wait for all the client threads to finish
        System.out.println("Waiting for client threads to finish...");
        for (int i = 0; i < threadCount; i++) {
            threads[i].join();
        }

        // Get the balance of each account
        System.out.println("Verifying post-threading-transfer balance...");
        Timer timer = new Timer();
        for(int sid = 0; sid < servers.length; sid++) {
            int total = 0;
            IBankServer serv = servers[sid];
            for (int i = 1; i < 21; i++) {
                // Logging and timing
                Printer.print("MAIN | Server-" + sid + " | REQ | " + LocalDateTime.now() + " | GET_BALANCE | account=" + i, Printer.File.CLIENT, "", "#e3b28a");
                timer.start();
                // Send the request and get the response
                Response res = serv.clientRequest((new Request()).ofType(Request.Type.GET_BALANCE).withUid(i).withOrigin("MAIN"));
                // Logging and timing
                timer.stop();
                Printer.print("MAIN | Server-" + sid + " | RES | " + LocalDateTime.now() + " | GET_BALANCE | time=" + timer.getTime() + "s, account=" + i + ", balance=" + res.getBalance(), Printer.File.CLIENT, "", "#b2f7b9");
                timer.clear();
                total += res.getBalance();
            }
            Printer.print("MAIN | Server-" + sid + " | | " + LocalDateTime.now() + " | TOTAL | balance=" + total, Printer.File.CLIENT, "", "#b2b7f7");
        }        
        // Log the average get balance time
        Printer.print("MAIN | | | " + LocalDateTime.now() + " | REPORT | avg get balance time=" + timer.getAverage(), Printer.File.CLIENT, "", "#737bf0");

        // Send a halt message to Server0
        Printer.print("MAIN | Server-0 | REQ | " + LocalDateTime.now() + " | HALT | ", Printer.File.CLIENT, "", "#e3b28a");
        System.out.println("Sending halt message to Server-0...");
        timer.start();
        try {
            servers[0].clientRequest((new Request()).ofType(Request.Type.HALT).withOrigin("MAIN"));
        } 
        // Ignore this exception
        catch(Exception e) {}
        // Logging and timing for the halt message
        timer.stop();
        Printer.print("MAIN | Server-0 | RES | " + LocalDateTime.now() + " | HALT | time=" + timer.getTime() + "s, success=true", Printer.File.CLIENT, "", "#b2f7b9");
        timer.clear();

        // Close the client and clean up
        System.out.println("Client has finished.");
        Printer.closeHtmlLog(Printer.File.CLIENT, "");
    }
}
