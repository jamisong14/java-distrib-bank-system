/**
 * Authors:
 *  - Jamison Grudem(grude013)
 *  - Manan Mrig (mrig0001)
 */

import java.io.File;
import java.rmi.Naming;
import java.time.LocalDateTime;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

class ClientThread extends Thread {
    
    private int iterationCount;

    public ClientThread(int iterationCount) {
        this.iterationCount = iterationCount;
    }

    public void run() {
        try {
            for(int i = 0; i < 200; i++) {
                // Load the configuration file
                File file = new File("config.xml");
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document configDoc = db.parse(file);

                // Choose a random server 
                int serverCount = configDoc.getElementsByTagName("hostname").getLength();
                int serverId = (int) (Math.random() * serverCount);
                String host = configDoc.getElementsByTagName("hostname").item(serverId).getTextContent();
                int port = Integer.parseInt(configDoc.getElementsByTagName("port").item(serverId).getTextContent());
                
                // Connect to the server
                IBankServer server = (IBankServer) Naming.lookup("//" + host + ":" + port + "/BankServer");

                // Transfer money between two random accounts
                int from = (int) (Math.random() * 20) + 1;
                int to = (int) (Math.random() * 20) + 1;
                while(to == from) {
                    to = (int) (Math.random() * 20) + 1;
                }

                // Build the request
                Request req = (new Request()).ofType(Request.Type.TRANSFER).from(from).to(to).withAmount(10);

                // Send the request and get the response
                Printer.print(Thread.currentThread().getId() + " | " + serverId + " | REQ | " + LocalDateTime.now() + " | TRANSFER | from=" + from + ", to=" + to + ", amount=10", Printer.File.CLIENT, "");
                server.clientRequest(req, "Thread-" + Thread.currentThread().getId());
                Printer.print(Thread.currentThread().getId() + " | " + serverId + " | RES | " + LocalDateTime.now() + " | success=" + true, Printer.File.CLIENT, "");

                Thread.sleep(1000);
            }
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

        // IBankServer server0 = (IBankServer) Naming.lookup ("//localhost:8013/BankServer");
        // Printer.print("[MAIN THREAD] Connected to server", Printer.File.CLIENT, "");

        // IBankServer server1 = (IBankServer) Naming.lookup ("//localhost:8014/BankServer");
        // Printer.print("[MAIN THREAD] Connected to server", Printer.File.CLIENT, "");

        // IBankServer server2 = (IBankServer) Naming.lookup ("//localhost:8015/BankServer");
        // Printer.print("[MAIN THREAD] Connected to server", Printer.File.CLIENT, "");

        // Response transferRes = server0.receive((new Request()).ofType(Request.Type.TRANSFER).from(18).to(19).withAmount(100), "Client", true);
        // System.out.println("Transfer: " + transferRes.getSuccess());

        // // Connect to the server
        // for(int port = 0; port < 3; port++) {
        //     // Get the balance of each account
        //     Printer.print("[MAIN THREAD] Verifying pre-threading-transfer balance", Printer.File.CLIENT, "");
        //     int total = 0;
        //     for (int i = 1; i < 21; i++) {
        //         Response res = server0.receive((new Request()).ofType(Request.Type.GET_BALANCE).withUid(i), "Client", true);
        //         int bal0 = res.getBalance();
        //         total += bal0;
        //     }
        //     Printer.print("[MAIN THREAD] Total Balance: " + total, Printer.File.CLIENT, "");
        // }

        // for(int i = 0; i < 200; i++) {
        //     // Choose a random server 
        //     int serverCount = configDoc.getElementsByTagName("hostname").getLength();
        //     int serverId = (int) (Math.random() * serverCount);
        //     String host = configDoc.getElementsByTagName("hostname").item(serverId).getTextContent();
        //     int port = Integer.parseInt(configDoc.getElementsByTagName("port").item(serverId).getTextContent());
            
        //     // Connect to the server
        //     System.out.println("Connecting to server: " + host + ":" + port);
        //     IBankServer server = (IBankServer) Naming.lookup("//" + host + ":" + port + "/BankServer");

        //     // Transfer money between two random accounts
        //     int from = (int) (Math.random() * 20) + 1;
        //     int to = (int) (Math.random() * 20) + 1;
        //     while(to == from) {
        //         to = (int) (Math.random() * 20) + 1;
        //     }

        //     // Build the request
        //     Request req = (new Request()).ofType(Request.Type.TRANSFER).from(from).to(to).withAmount(10);

        //     // Send the request and get the response
        //     Printer.print(Thread.currentThread().getId() + " | " + serverId + " | REQ | " + LocalDateTime.now() + " | TRANSFER | from=" + from + ", to=" + to + ", amount=10", Printer.File.CLIENT, "");
        //     Response res = server.receive(req, "Thread-" + Thread.currentThread().getId(), true);
        //     Printer.print(Thread.currentThread().getId() + " | " + serverId + " | RES | " + LocalDateTime.now() + " | success=" + res.getSuccess(), Printer.File.CLIENT, "");
        // }

        // Create and start the client threads
        System.out.println("[MAIN THREAD] Creating and starting client threads");
        ClientThread[] threads = new ClientThread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new ClientThread(200);
            threads[i].start();
        }
        
        // Wait for all the client threads to finish
        System.out.println("[MAIN THREAD] Waiting for client threads to finish");
        for (int i = 0; i < threadCount; i++) {
            threads[i].join();
        }

        // Get the balance of each account
        IBankServer server0 = (IBankServer) Naming.lookup("//localhost:8013/BankServer");
        Printer.print("[MAIN THREAD] Verifying post-threading-transfer balance", Printer.File.CLIENT, "");
        int total = 0;
        for (int i = 1; i < 21; i++) {
            int bal = server0.getBalance(i);
            Printer.print("[MAIN THREAD] Balance of " + i + ": " + bal, Printer.File.CLIENT, "");
            total += bal;
        }
        Printer.print("[MAIN THREAD] Total Balance: " + total, Printer.File.CLIENT, "");
    }
}
