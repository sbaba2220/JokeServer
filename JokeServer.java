/* 2.0 2024-04-07 Saibaba Garbham, JokeServer.java, JokeClient.java and JokeClientAdmin.java
Copyright (c) 2023 by Saibaba Garbham with all rights reserved.

Name: Saibaba Garbham
Date: 2023-04-07
Java Version: 20 (build 20+36-2344)
Command-line compilation: > javac *.java [executed twice]

Running these programs:
We can run Client and Server on separate terminal windows on a same machine in multiple processes.
We can also run on different machines across the internet by passing the host as an argument to ColorClient program.

To run on localhost:

Terminal/CMD window 1> java ColorServer
Terminal/CMD window 2> java ColorClient
Terminal/CMD window 3> java ColorClient
[...]
Terminal/CMD window N> java ColorClient

Alternatively, to run over the Internet:

Terminal/CMD window 1> java ColorServer
Terminal/CMD window 2> java ColorClient 172.16.0.98 [But use the actual IP address of the ColorServer]
[...]

Files needed: JokeServer.java

Notes:

We build a client server program in which they talk to each other using TCP/IP. We have ColorServer that needs to run
first and look for the clients in port 45565. And our ColorClient run simultaneously on port 45565 and asks ths user for
a color and open a connection to the server and sends that color to the server. Our server uses ColorWorker and handles
the request from client and sends back a new random color back to client. We can have multiple clients and our ColorWorker
handles the requests from multiple clients.

ColorServer:
    Our ColorServer will create a socket connection and wait for the clients to send a request.
    When a client sends a request, then the server will accept it and pass it to the ColorWorker class which is extending a Thread.
    We are starting the thread after receiving a message from Client. ColorWorker will read the message from client and generates a new random color from an existing color array and pass it back to client.

ColorClient:
    ColorClient class will take the inputs from the user. It will ask for the user name and the color the user wants to send to the server.
    Loop continuously and take inputs from the user until the user enters "quit".
    Once a text other than "quit" is entered as Color. Our Client will create a Socket using port 45565 and pass the color information to the server.
    The client will listen for the information from Server and store the state information in a variable.
    Displays the state information (Color count and Colors sent back and forth) to the user once the user chooses to quit.

--------------------

*/

import java.io.*;
import java.net.*;
import java.util.*;

class JokeClient {
    private static String userName;
    private int jokeIndex;
    private int proverbIndex;
    private int clientId;
    private String jokeOrProverbFromServer;

    public static void main(String argv[]) {
        // Creating jokeClient object here
        JokeClient jokeClient = new JokeClient(argv);
        jokeClient.run(argv); // Calling the run method
    }

    public JokeClient(String args[]) {
        //System.out.println("Constructor of JokeClient");
    }

    public void run(String args[]) {
        String servername;
        // Setting the servername from args
        if (args.length < 1) {
            servername = "localhost";
        } else {
            servername = args[0];
        }
        Scanner consoleIn = new Scanner(System.in);
        // Take username from User
        System.out.print("Enter your name: ");
        System.out.flush();
        userName = consoleIn.nextLine();
        jokeIndex = 0;
        proverbIndex = 0;
        clientId = 0;
        System.out.println("Hi " + userName);
        String userInput;
        do {
            // Asking the user to press Enter to get a joke or proverb based on the server mode.
            System.out.print("Press \"Enter\" to get a joke or proverb, or quit to end: ");
            userInput = consoleIn.nextLine();
            if (userInput != null) {
                if (userInput.indexOf("quit") < 0) { // Client pressed Enter
                    getJokeOrProverb(userName, servername);
                }
            }
        } while (userInput.indexOf("quit") < 0); // Look for joke or proverb until user enters quit
        System.out.println("Cancelled by user request. Thank you!");
    }

    void getJokeOrProverb(String userName, String serverName) {
        try {
            // Setting the NetworkDate here
            NetworkData networkData = new NetworkData();
            networkData.userName = userName;
            networkData.jokeIndex = jokeIndex;
            networkData.clientId = clientId;
            networkData.proverbIndex = proverbIndex;

            // Creating the socket connection below
            // Socket socket = new Socket("UNKNOWNHOST", 45565); // Demonstrate the UH exception below.
            int primaryPort = 4545;
            Socket socket = new Socket(serverName, primaryPort);
            //System.out.println("\n  Successful connection established with JokeServer on port " + primaryPort);

            OutputStream networkStream = socket.getOutputStream(); // Get network output stream from the socket
            ObjectOutputStream networkStreamObject = new ObjectOutputStream(networkStream); // Object serialization goes here

            networkStreamObject.writeObject(networkData); // Pass the serialized object to the server
            //System.out.println("    Successfully sent the serialized values to the JokeServer's server socket");

            InputStream networkInStream = socket.getInputStream(); // Get network input stream from joke server
            ObjectInputStream objectInputStream = new ObjectInputStream(networkInStream);
            NetworkData inObject = (NetworkData) objectInputStream.readObject(); // Reading the input stream from joke server

            // Assigning the joke or proverb and indexes of joke/proverb to state variables
            jokeOrProverbFromServer = inObject.jokeOrProverbSentBack;
            jokeIndex = inObject.jokeIndex;
            proverbIndex = inObject.proverbIndex;
            clientId = inObject.clientId;

            // Displaying joke/proverb information here
            System.out.println(inObject.jokeOrProverbKey + " " + userName + ": " + inObject.jokeOrProverbSentBack);
            // Displaying the cycle ends message below
            if (!inObject.messageToClient.isEmpty()) {
                System.out.println("### " + inObject.messageToClient + " ###");
            }
            //System.out.println("Closing the connection to the server.\n");
            socket.close(); // Closing the socket connection
        } catch (ConnectException connectException) {
            System.out.println("\nOh no. The JokeServer refused our connection! Is it running?\n");
            connectException.printStackTrace();
        } catch (UnknownHostException unknownHostException) {
            System.out.println("\nUnknown Host problem.\n");
            unknownHostException.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (ClassNotFoundException classNotFoundException) {
            classNotFoundException.printStackTrace();
        }
    }
}

class JokeClientAdmin {
    private String currentMode;

    public static void main(String argv[]) {
        // Creating jokeClientAdmin object here
        JokeClientAdmin jokeClientAdmin = new JokeClientAdmin(argv);
        jokeClientAdmin.run(argv); // Starting here
    }

    public JokeClientAdmin(String args[]) {
        System.out.println("JokeClientAdmin constructor");
    }

    public void run(String args[]) {
        String servername;
        // Reading args and setting the servername
        if (args.length < 1) {
            servername = "localhost";
        } else {
            servername = args[0];
        }
        Scanner consoleIn = new Scanner(System.in);
        // Take inputs from user
        System.out.print("Hello Joke Client Admin");
        System.out.println("    The initial Joke Server mode is \"Joke\"");
        String userInput;
        do {
            System.out.print("      Press \"Enter\" to change Server mode, or quit to end: ");
            userInput = consoleIn.nextLine();
            if (userInput != null) {
                if (userInput.indexOf("quit") < 0) { // Joke Client Admin pressed Enter
                    connectToServer(servername);
                }
            }
        } while (userInput.indexOf("quit") < 0); // Taking the joke client admin input until the admin enters quit
        System.out.println("Cancelled by user request.");
    }

    void connectToServer(String serverName) {
        try {
            Socket socket = new Socket(serverName, 5050);
            //System.out.println("\n          Successful connection established with JokeClientAdmin at port 5050");

            InputStream inStream = socket.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inStream);
            AdminData inObject = (AdminData) objectInputStream.readObject();

            currentMode = inObject.mode;
            System.out.println("            Mode changed to : " + inObject.mode);
            System.out.println("         Closing the connection to the server.\n");
            socket.close();
        } catch (ConnectException connectException) {
            System.out.println("\nOh no. The JokeServer refused our connection! Is it running?\n");
            connectException.printStackTrace();
        } catch (UnknownHostException unknownHostException) {
            System.out.println("\nUnknown Host problem.\n");
            unknownHostException.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (ClassNotFoundException classNotFoundException) {
            classNotFoundException.printStackTrace();
        }
    }
}

// AdminData class to maintain the mode
class AdminData implements Serializable {
    String mode;
}

// NetworkData class to maintain the necessary data between client and server
class NetworkData implements Serializable {
    int clientId;
    String userName;
    int jokeIndex;
    int proverbIndex;
    String jokeOrProverbSentBack;
    String jokeOrProverbKey;
    String messageToClient;
}

class JokeWorker extends Thread {
    Socket socket;
    ModeChanger mode;
    private int limiter;
    private int currentIndex;
    private NetworkData inObject;
    private ClientData clientData;

    private Dictionary<String, String> jokes = new Hashtable<>();
    private Dictionary<String, String> proverbs = new Hashtable<>();

    JokeWorker(Socket s, ModeChanger mode, Dictionary<String, String> jokes, Dictionary<String, String> proverbs, ClientData clientData, NetworkData networkData) {
        this.socket = s;
        this.mode = mode;
        this.jokes = jokes;
        this.proverbs = proverbs;
        this.limiter = 4;
        this.inObject = networkData;
        this.clientData = clientData;
    }

    public void run() {
        try {

            OutputStream outputStream = socket.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

            System.out.println("Request from Username : " + inObject.userName);
            clientData.setJokeCycleCompleted(false);
            clientData.setProverbCycleCompleted(false);
            inObject.messageToClient = "";
            if (this.mode.GetMode() == 0) {
                this.currentIndex = inObject.jokeIndex;
                inObject.jokeIndex++;
                if (inObject.jokeIndex == this.limiter) {
                    inObject.jokeIndex = 0;
                    inObject.messageToClient = "JOKE CYCLE COMPLETED";
                    clientData.setJokeCycleCompleted(true);
                }
            } else {
                this.currentIndex = inObject.proverbIndex;
                inObject.proverbIndex++;
                if (inObject.proverbIndex == this.limiter) {
                    inObject.proverbIndex = 0;
                    inObject.messageToClient = "PROVERB CYCLE COMPLETED";
                    clientData.setProverbCycleCompleted(true);
                }
            }
            setJokeOrProverb(currentIndex, mode.GetMode(), inObject);
            System.out.println("CLIENT ID IN JokeWorker " + clientData.getId());
            inObject.clientId = clientData.getId();
            objectOutputStream.writeObject(inObject); // Send the data back to client
            System.out.println("    Request successfully processed");
            //System.out.println("Closing the client socket connection...");
            socket.close();

        } catch (IOException inpIoException) {
            System.out.println("Server error.");
            inpIoException.printStackTrace();
        }
    }

    void setJokeOrProverb(int index, int mode, NetworkData inObject) {
        if (mode == 0) {
            if (index == 0) {
                inObject.jokeOrProverbKey = "JA";
                inObject.jokeOrProverbSentBack = jokes.get("JA");
            } else if (index == 1) {
                inObject.jokeOrProverbKey = "JB";
                inObject.jokeOrProverbSentBack = jokes.get("JB");
            } else if (index == 2) {
                inObject.jokeOrProverbKey = "JC";
                inObject.jokeOrProverbSentBack = jokes.get("JC");
            } else if (index == 3) {
                inObject.jokeOrProverbKey = "JD";
                inObject.jokeOrProverbSentBack = jokes.get("JD");
            }
        } else {
            if (index == 0) {
                inObject.jokeOrProverbKey = "PA";
                inObject.jokeOrProverbSentBack = proverbs.get("PA");
            } else if (index == 1) {
                inObject.jokeOrProverbKey = "PB";
                inObject.jokeOrProverbSentBack = proverbs.get("PB");
            } else if (index == 2) {
                inObject.jokeOrProverbKey = "PC";
                inObject.jokeOrProverbSentBack = proverbs.get("PC");
            } else if (index == 3) {
                inObject.jokeOrProverbKey = "PD";
                inObject.jokeOrProverbSentBack = proverbs.get("PD");
            }
        }
        System.out.println("    Sent: " + inObject.jokeOrProverbKey);
    }
}

class ModeChanger {
    int mode = 0;

    public int ChangeMode() {
        if (mode == 0) {
            mode = 1;
        } else {
            mode = 0;
        }
        return (mode);
    }

    public int GetMode() {
        return (mode);
    }

}

class AdminLooper implements Runnable {
    public static boolean adminControlSwitch = true;
    ModeChanger mode = new ModeChanger();

    public void run() {

        int q_len = 6; /* Number of requests for OpSys to queue */
        int port = 5050;  // We are listening at a different port for Admin clients
        Socket sock;

        try {
            ServerSocket servsock = new ServerSocket(port, q_len);
            while (adminControlSwitch) {
                // wait for the next ADMIN client connection:
                sock = servsock.accept();
                new JokeClientAdminWorker(sock, mode).start();
            }
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }
}

class JokeClientAdminWorker extends Thread { // Class definition. Extending Thread because these worker threads may run simultaneously
    Socket socket;
    ModeChanger mode;

    JokeClientAdminWorker(Socket s, ModeChanger mode) {
        this.socket = s;
        this.mode = mode;
    }

    public void run() {
        try {
            AdminData inObject = new AdminData();

            OutputStream outputStream = socket.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

            mode.ChangeMode();
            if (mode.GetMode() == 0) {
                inObject.mode = "Joke";
            } else {
                inObject.mode = "Proverb";
            }

            objectOutputStream.writeObject(inObject); // Send the admin data back to client
            System.out.println("Mode changed to : " + inObject.mode);
            System.out.println("Closing the client socket connection...");
            socket.close();

        } catch (IOException inpIoException) {
            System.out.println("Server error.");
            inpIoException.printStackTrace();
        }
    }
}

public class JokeServer {

    private static Dictionary<String, String> jokes = new Hashtable<>();
    private static Dictionary<String, String> proverbs = new Hashtable<>();
    private static String[] initJokes = new String[]{"Joke A","Joke B","Joke C","Joke D"};
    private static String[] initProverbs = new String[]{"Proverb A","Proverb B","Proverb C","Proverb D"};
    private static List<ClientData> clients = new ArrayList<>();
    private static int clientId = 1;

    public static void main(String[] args) throws Exception {
        int queueLen = 6; // Number of simultaneous requests for Operating System to queue
        int serverPort = 4545;
        Socket socket;
        System.out.println("Saibaba Garbham's Joke Server 1.0 starting up, listening for Joke Client at port  " + serverPort + ".\n");

        ServerSocket serverSocket = new ServerSocket(serverPort, queueLen);
        System.out.println("ServerSocket awaiting connections..."); // Waiting for the client to ring the bell

        AdminLooper AL = new AdminLooper();
        Thread t = new Thread(AL);
        t.start();

        while (true) { // Use Ctrl C to manually terminate the server
            socket = serverSocket.accept(); // Answer the client connection
            System.out.println("Connection from " + socket);
            InputStream inputStream = socket.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            NetworkData inObject = (NetworkData) objectInputStream.readObject();
            ClientData clientData = null;
            boolean isClientFound = false;
            System.out.println();
            System.out.println("Clients length "+clients.size());
            for (ClientData client :
                    clients) {
                if (inObject.clientId == client.getId()) {
                    clientData = client;
                    isClientFound = true;
                }
            }
            System.out.println("Client found? "+isClientFound);
            if (!isClientFound) {
                clientData = new ClientData(clientId, inObject.userName);
                clients.add(clientData);
                clientId++;
            }
            System.out.println("InObject Client info "+inObject.clientId+"  ##  "+clients.get(0).getId());
            System.out.println("ClientID "+clientData.getId());
            if (clientData.getIsJokeCycleCompleted()) {
                randomizeJokes(clientData);
            }
            if (clientData.getIsProverbCycleCompleted()) {
                randomizeProverbs(clientData);
            }
            LoadJokesAndProverbs(clientData);
            System.out.println("Client Data "+clientData.getId()+clientData.getName());
            System.out.println("Client JOKE AND PROVERB CYCLE DATA"+clientData.getIsJokeCycleCompleted() + clientData.getIsProverbCycleCompleted());
            System.out.println("JOKES ARE " + jokes.get("JA") + jokes.get("JB") + jokes.get("JC") + jokes.get("JD"));
            new JokeWorker(socket, AL.mode, jokes, proverbs, clientData, inObject).start();
        }
    }

    public static void randomizeJokes(ClientData clientData) {
        int[] jokeOrder = new int[] {2,3,4,1};
        clientData.setJokeOrder(jokeOrder);
    }

    public static void randomizeProverbs(ClientData clientData) {
        int[] proverbOrder = new int[] {2,3,4,1};
        clientData.setProverbOrder(proverbOrder);
    }

    public static void LoadJokesAndProverbs(ClientData clientData) {
        jokes = new Hashtable<>();
        proverbs = new Hashtable<>();
        int[] jokeOrder = clientData.getJokeOrder();
        int[] proverbOrder = clientData.getProverbOrder();
        jokes.put("JA", initJokes[jokeOrder[0]-1]);
        jokes.put("JB", initJokes[jokeOrder[1]-1]);
        jokes.put("JC", initJokes[jokeOrder[2]-1]);
        jokes.put("JD", initJokes[jokeOrder[3]-1]);
        proverbs.put("PA", initProverbs[proverbOrder[0]-1]);
        proverbs.put("PB", initProverbs[proverbOrder[1]-1]);
        proverbs.put("PC", initProverbs[proverbOrder[2]-1]);
        proverbs.put("PD", initProverbs[proverbOrder[3]-1]);
    }
}

class ClientData {
    private int id;
    private String name;
    private boolean isJokeCycleCompleted;
    private boolean isProverbCycleCompleted;
    private int[] jokeOrder;
    private int[] proverbOrder;

    public ClientData(int id, String name) {
        this.id = id;
        this.name = name;
        this.isJokeCycleCompleted = false;
        this.isProverbCycleCompleted = false;
        this.jokeOrder = new int[] {1,2,3,4};
        this.proverbOrder = new int[] {1,2,3,4};
    }

    public int[] getJokeOrder() {
        return jokeOrder;
    }

    public void setJokeOrder(int[] jokeOrder) {
        this.jokeOrder = jokeOrder;
    }


    public int[] getProverbOrder() {
        return proverbOrder;
    }

    public void setProverbOrder(int[] proverbOrder) {
        this.proverbOrder = proverbOrder;
    }
    public void setJokeCycleCompleted(boolean isJokeCycleCompleted) {
        this.isJokeCycleCompleted = isJokeCycleCompleted;
    }

    public void setProverbCycleCompleted(boolean isProverbCycleCompleted) {
        this.isProverbCycleCompleted = isProverbCycleCompleted;
    }

    public boolean getIsJokeCycleCompleted() {
        return this.isJokeCycleCompleted;
    }

    public boolean getIsProverbCycleCompleted() {
        return this.isProverbCycleCompleted;
    }

    public String getName() {
        return this.name;
    }

    public int getId() {
        return this.id;
    }

}



/*

------------------------------------ OUTPUT ------------------------------------


----------------------------------------------------------

MY D2L COLORSERVER DISCUSSION FORUM POSTINGS:

Posting 1: Subject: ColorWorker Question
My Comment:
In Java, when we extend the Thread class, we should override the run() method.
This run method will be called when we start a thread. In ColorServer we are calling this as new ClassWorker(socket).start().

Posting 2: Subject: Client Sending Random colors
My Comment:
As per the code, the server is supposed to accept any color or any text the client is sending and return colors from the colors array.
*/