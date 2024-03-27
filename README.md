# Part B: RMI Based Server
Authors:
* Jamison Grudem (grude013)
* Manan Mrig (mrig0001)

## About The Program
This program is an RMI based client/server for a mock bank. Both the client and server are multi-threaded allowing for efficient handling of multiple clients and concurrent requests from the client.  

The server is able to handle 4 different operations:
1. Create a new account
    ```java
    /**
     * Create a new account on the server
     * @return The account number of the new account
     */
    public int createAccount();
    ```
2. Deposit money into an account
    ```java
    /**
     * Deposit money into an account
     * @param accountNumber The account number to deposit money into
     * @param amount The amount to deposit
     * @return True if the deposit was successful, false otherwise
     */
    public boolean deposit(int accountNumber, int amount);
    ```
3. Check the balance of an account
    ```java
    /**
     * Get the balance of an account
     * @param accountNumber The account number to check the balance of
     * @return The balance of the account if found, otherwise -1
     */
    public int getBalance(int accountNumber);
    ```
4. Transfer money between two accounts
    ```java
    /**
     * Transfer money between two accounts
     * @param fromAccount The account number to transfer money from
     * @param toAccount The account number to transfer money to
     * @param amount The amount to transfer
     * @return True if the transfer was successful, false otherwise
     */
    public boolean transfer(int fromAccount, int toAccount, int amount);
    ```

The declaration of these functions can be seen in `BankServer.java`, while the implementation of these functions can be viewed in `BankServerImpl.java`. The client, notably found in `BankClient.java`, calls these functions using the `BankServer` interface.

## How to Run
### Compiling The Program
To run the program, first compile the server and client files:
```bash
make
```
Alternatively, you can compile the server and client files manually using:
```bash
javac *.java
```
### Cleaning Compiled Files
If you want to clear all the compiled files (and log files), you can run:
```bash
make clean
```
Alternatively, you can remove the compiled files manually using:
```bash
rm *.class
rm clientOutput
rm clientLogfile
rm serverOutput
rm serverLogfile
```
### Running The Server
**Note: The server must be running before the client can connect to it.**  
To run the server, open up a new terminal and type the first command:
```bash
make runs

<== resolves to ==>
java BankServerImpl 1099
```
The make target for the server contains an optional parameter `p` to specify a port number. If no port number is specified, the default port number is 1099. Run the following command to specify a port number:
```bash
make runs p=<portNumber>
```
* **portNumber**: The port number the server should run on  

Alternatively, you can run the server manually using:
```bash
java BankServer <portNumber>
```
* **portNumber**: The port number the server should run on
### Running The Client
To run the client, open up a new terminal and type the first command:
```bash
make runc

<== resolves to ==>
java BankClient localhost:1099 100 100
```
The make target for the client contains three optional parameters: `p`, `t`, and `i`. `p` represents the server port number. `t` represents the number of client threads to create. `i` represents the number of iterations each thread should run. If no parameters are specified, the default values are `1099`, `100`, and `100` respectively. You can specify all three parameters or only one of them (as long as you use the correct prefix). Run the following command to specify these parameters:
```bash
make runc p=<portNumber> t=<threadCount> i=<iterationCount>
```
* **portNumber**: The port number the server is running on
* **threadCount**: The number of threads to run concurrently
* **iterationCount**: The number of transfer requests each thread should make  

Alternatively, you can run the client manually using:
```bash
java BankClient <serverAddress>:<portNumber> <threadCount> <iterationCount>
```
* **serverAddress**: The address of the server (e.g. localhost)
* **portNumber**: The port number the server is running on
* **threadCount**: The number of threads to run concurrently
* **iterationCount**: The number of transfer requests each thread should make

## Program Output
As per the assignment requirements, the server and client both output to a log file. Both the server and client use a utility class (Printer.java) which is used to print to the terminal and write to a file in one statment. The output files are as follows:
### Server Output
* **serverOutput**: The same output from the terminal is written to this file (for easier viewing)
* **serverLogfile**: The server logs all requests and responses to this file
### Client Output
* **clientOutput**: The same output from the terminal is written to this file (for easier viewing)
* **clientLogfile**: The client logs all failed transfer requests to this file

## Known Errors/Bugs
At this time, there are no known errors or bugs in the program.