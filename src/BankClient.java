/**
 * Authors:
 *  - Jamison Grudem(grude013)
 */
package src;

import java.io.File;
import java.rmi.Naming;
import java.time.LocalDateTime;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

class ClientThread extends Thread {

    private IBankServer[] servers;

    public ClientThread(IBankServer[] servers) {
        this.servers = servers;
    }

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

                // Send the request and get the response
                Printer.print("T-" + Thread.currentThread().getId() + " | Server-" + serverId + " | REQ | " + LocalDateTime.now() + " | TRANSFER | from=" + from + ", to=" + to + ", amount=10", Printer.File.CLIENT, "", "#e3b28a");
                timer.start();
                Response res = server.clientRequest(req);
                timer.stop();
                Printer.print("T-" + Thread.currentThread().getId() + " | Server-" + serverId + " | RES | " + LocalDateTime.now() + " | " + res.getType() + " | time=" + timer.getTime() + "s, success=" + res.getSuccess() + ", timestamp=" + res.getClock(), Printer.File.CLIENT, "", "#b2f7b9");
                timer.clear();

                // TESTING: Sleep for 1 second
                // Thread.sleep(1000);
            }

            Printer.print("T-" + Thread.currentThread().getId() + " | | | " + LocalDateTime.now() + " | REPORT | avg transfer time=" + timer.getAverage(), Printer.File.CLIENT, "", "#737bf0");
        } catch (Exception e) {
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
        int serverCount = configDoc.getElementsByTagName("server").getLength();
        String serverLinks = "";
        for (int i = 0; i < serverCount; i++) {
            String configSid = configDoc.getElementsByTagName("id").item(i).getTextContent();
            serverLinks += "<a href='server" + configSid + ".html'>Server-" + configSid + " Log</a> | ";
        }
        Printer.initHtmlLog(Printer.File.CLIENT, "", serverLinks);
        Printer.print("MAIN | | START | " + LocalDateTime.now(), Printer.File.CLIENT, "", "#b2b7f7");

        // Connect to all the servers - and store them in an array
        IBankServer[] servers = new IBankServer[serverCount];
        for (int i = 0; i < serverCount; i++) {
            String host = configDoc.getElementsByTagName("hostname").item(i).getTextContent();
            int port = Integer.parseInt(configDoc.getElementsByTagName("port").item(i).getTextContent());
            Printer.print("MAIN | Server-" + i + " | REQ | " + LocalDateTime.now() + " | CONNECT | " + host + ":" + port, Printer.File.CLIENT, "", "#e3b28a");
            servers[i] = (IBankServer) Naming.lookup("//" + host + ":" + port + "/BankServer");
            Printer.print("MAIN | Server-" + i + " | RES | " + LocalDateTime.now() + " | | success=" + true, Printer.File.CLIENT, "", "#b2f7b9");
        }

        // Create and start the client threads
        System.out.println("[MAIN THREAD] Creating and starting client threads");
        ClientThread[] threads = new ClientThread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new ClientThread(servers);
            threads[i].start();
        }
        
        // Wait for all the client threads to finish
        System.out.println("[MAIN THREAD] Waiting for client threads to finish");
        for (int i = 0; i < threadCount; i++) {
            threads[i].join();
        }

        // Get the balance of each account
        System.out.println("[MAIN THREAD] Verifying post-threading-transfer balance");
        Timer timer = new Timer();
        for(int sid = 0; sid < servers.length; sid++) {
            int total = 0;
            IBankServer serv = servers[sid];
            for (int i = 1; i < 21; i++) {
                Printer.print("MAIN | Server-" + sid + " | REQ | " + LocalDateTime.now() + " | GET_BALANCE | account=" + i, Printer.File.CLIENT, "", "#e3b28a");
                timer.start();
                Response res = serv.clientRequest((new Request()).ofType(Request.Type.GET_BALANCE).withUid(i).withOrigin("MAIN"));
                timer.stop();
                Printer.print("MAIN | Server-" + sid + " | RES | " + LocalDateTime.now() + " | GET_BALANCE | time=" + timer.getTime() + "s, account=" + i + ", balance=" + res.getBalance(), Printer.File.CLIENT, "", "#b2f7b9");
                timer.clear();
                total += res.getBalance();
            }
            Printer.print("MAIN | Server-" + sid + " | | " + LocalDateTime.now() + " | TOTAL | balance=" + total, Printer.File.CLIENT, "", "#b2b7f7");
        }        
        Printer.print("MAIN | | | " + LocalDateTime.now() + " | REPORT | avg get balance time=" + timer.getAverage(), Printer.File.CLIENT, "", "#737bf0");

        // Send a halt message to Server0
        Printer.print("MAIN | Server-0 | REQ | " + LocalDateTime.now() + " | HALT | ", Printer.File.CLIENT, "", "#e3b28a");
        timer.start();
        try {
            servers[0].clientRequest((new Request()).ofType(Request.Type.HALT).withOrigin("MAIN"));
        } catch(Exception e) {
            // Ignore this exception
        }
        timer.stop();
        Printer.print("MAIN | Server-0 | RES | " + LocalDateTime.now() + " | HALT | time=" + timer.getTime() + "s, success=true", Printer.File.CLIENT, "", "#b2f7b9");
        timer.clear();

        Printer.closeHtmlLog(Printer.File.CLIENT, "");
    }
}
