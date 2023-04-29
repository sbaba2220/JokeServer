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
    private UUID clientId;
    private int s1JokeIndex;
    private int s2JokeIndex;
    private int s1ProverbIndex;
    private int s2ProverbIndex;
    private String jokeOrProverbFromServer;

    public static void main(String argv[]) {
        // Creating jokeClient object here
        JokeClient jokeClient = new JokeClient(argv);
        jokeClient.run(argv); // Calling the run method
    }

    public JokeClient(String args[]) {
        // JokeClient Constructor
    }

    public void run(String args[]) {
        String firstServername = "";
        String secondServername = "";
        String mainServername = "";
        boolean isSecondServer = false;
        boolean isSecondServerExists = false;
        int firstPort = 0;
        int secondPort = 0;
        int mainPort = 0;
        // Setting the servername from args
        if (args.length < 1) {
            firstServername = "localhost";
            firstPort = 4545;
        } else if (args.length == 1) {
            firstServername = args[0];
            firstPort = 4545;
        } else if (args.length == 2) {
            firstServername = args[0];
            firstPort = 4545;
            secondPort = 4546;
            secondServername = args[1];
            isSecondServerExists = true;
        }
        Scanner consoleIn = new Scanner(System.in);
        // Take username from User
        System.out.print("Enter your name: ");
        System.out.flush();
        userName = consoleIn.nextLine();
        setDefaultValues();
        System.out.println("Hi " + userName);
        String userInput;
        do {
            // Asking the user to press Enter to get a joke or proverb based on the server mode.
            System.out.print("Press \"Enter\" to get a joke or proverb, or \"s\" to switch between servers, or quit to end: ");
            userInput = consoleIn.nextLine();
            if (userInput != null) {
                if (userInput.indexOf("s") >= 0) {
                    if (isSecondServerExists) {
                        isSecondServer = !isSecondServer;
                        System.out.println("Connected to second server!");
                    } else {
                        System.out.println("No secondary server being used");
                    }
                } else if (userInput.indexOf("quit") < 0) { // Client pressed Enter
                    if (!isSecondServer) {
                        mainServername = firstServername;
                        mainPort = firstPort;
                    } else {
                        mainServername = secondServername;
                        mainPort = secondPort;
                    }
                    getJokeOrProverb(userName, mainServername, mainPort, isSecondServer);
                }
            }
        } while (userInput.indexOf("quit") < 0); // Look for joke or proverb until user enters quit
        System.out.println("Cancelled by user request. Thank you!");
    }

    void setDefaultValues() {
        clientId = UUID.randomUUID();
        s1JokeIndex = 0;
        s1ProverbIndex = 0;
        s2JokeIndex = 0;
        s2ProverbIndex = 0;
    }

    void getJokeOrProverb(String userName, String serverName, int port, boolean isSecondServer) {
        try {
            // Setting the NetworkDate here
            NetworkData networkData = new NetworkData();
            networkData.userName = userName;
            networkData.clientId = clientId;
            if (!isSecondServer) {
                networkData.jokeIndex = s1JokeIndex;
                networkData.proverbIndex = s1ProverbIndex;
            } else {
                networkData.jokeIndex = s2JokeIndex;
                networkData.proverbIndex = s2ProverbIndex;
            }

            // Creating the socket connection below
            Socket socket = new Socket(serverName, port);

            OutputStream networkStream = socket.getOutputStream(); // Get network output stream from the socket
            ObjectOutputStream networkStreamObject = new ObjectOutputStream(networkStream); // Object serialization goes here

            networkStreamObject.writeObject(networkData); // Pass the serialized object to the server

            InputStream networkInStream = socket.getInputStream(); // Get network input stream from joke server
            ObjectInputStream objectInputStream = new ObjectInputStream(networkInStream);
            NetworkData inObject = (NetworkData) objectInputStream.readObject(); // Reading the input stream from joke server

            // Assigning the joke or proverb and indexes of joke/proverb to state variables
            jokeOrProverbFromServer = inObject.jokeOrProverbSentBack;
            if (!isSecondServer) {
                s1JokeIndex = inObject.jokeIndex;
                s1ProverbIndex = inObject.proverbIndex;
            } else {
                s2JokeIndex = inObject.jokeIndex;
                s2ProverbIndex = inObject.proverbIndex;
            }

            // Displaying joke/proverb information here
            System.out.println(inObject.jokeOrProverbSentBack);
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

public class JokeServer {

    private static LinkedHashMap<String, String> jokes = new LinkedHashMap<>();
    private static LinkedHashMap<String, String> proverbs = new LinkedHashMap<>();
    private static LinkedHashMap<String, String> initJokes = new LinkedHashMap<>();
    private static LinkedHashMap<String, String> initProverbs = new LinkedHashMap<>();
    private static List<ClientData> clients = new ArrayList<>();
    private static int maxJokesOrProverbs = 4;

    public static void main(String[] args) throws Exception {
        int queueLen = 6; // Number of simultaneous requests for Operating System to queue
        int serverPort = 4545;
        if(args.length == 1) {
            serverPort = 4546;
        }
        Socket socket;
        System.out.println("Saibaba Garbham's Joke Server 1.0 starting up, listening for Joke Client at port  " + serverPort + ".\n");
        LoadInitJokesAndProverbs();
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

            for (ClientData client :
                    clients) {
                if (inObject.clientId.equals(client.getId())) {
                    clientData = client;
                    isClientFound = true;
                }
            }
            if (!isClientFound) {
                clientData = new ClientData(inObject.clientId, inObject.userName);
                clients.add(clientData);
            }
            if (clientData.getIsJokeCycleCompleted()) {
                randomizeJokes(clientData);
            }
            if (clientData.getIsProverbCycleCompleted()) {
                System.out.println("RANDOMIZING");
                randomizeProverbs(clientData);
            }
            LoadJokesAndProverbs(clientData);
            new JokeWorker(socket, AL.mode, jokes, proverbs, clientData, inObject).start();
        }
    }

    public static void LoadInitJokesAndProverbs() {
        initJokes.put("JA", "Joke A");
        initJokes.put("JB", "Joke B");
        initJokes.put("JC", "Joke C");
        initJokes.put("JD", "Joke D");
        initProverbs.put("PA", "Proverb A");
        initProverbs.put("PB", "Proverb B");
        initProverbs.put("PC", "Proverb C");
        initProverbs.put("PD", "Proverb D");
    }

    public static void randomizeJokes(ClientData clientData) {
        List<Integer> shuffledArray = new ArrayList<>();
        for (int ind = 1; ind <= maxJokesOrProverbs; ind++) {
            shuffledArray.add(ind);
        }
        Collections.shuffle(shuffledArray);
        clientData.setJokeOrder(shuffledArray.stream().mapToInt(each -> each).toArray());
    }

    public static void randomizeProverbs(ClientData clientData) {
        List<Integer> shuffledArray = new ArrayList<>();
        for (int ind = 1; ind <= maxJokesOrProverbs; ind++) {
            shuffledArray.add(ind);
        }
        Collections.shuffle(shuffledArray);
        clientData.setProverbOrder(shuffledArray.stream().mapToInt(each -> each).toArray());
    }

    public static void LoadJokesAndProverbs(ClientData clientData) {
        jokes = new LinkedHashMap<>();
        proverbs = new LinkedHashMap<>();
        int[] jokeOrder = clientData.getJokeOrder();
        int[] proverbOrder = clientData.getProverbOrder();
        List<String> jokeKeys = new ArrayList<>(initJokes.keySet());
        List<String> proverbKeys = new ArrayList<>(initProverbs.keySet());
        for (int ind = 0; ind < jokeOrder.length; ind++) {
            String key = jokeKeys.get(jokeOrder[ind] - 1);
            jokes.put(key, initJokes.get(key));
        }
        for (int ind = 0; ind < proverbOrder.length; ind++) {
            String key = proverbKeys.get(proverbOrder[ind] - 1);
            proverbs.put(key, initProverbs.get(key));
        }
    }
}

class JokeWorker extends Thread {
    Socket socket;
    ModeChanger mode;
    private int limiter;
    private int currentIndex;
    private NetworkData inObject;
    private ClientData clientData;

    private LinkedHashMap<String, String> jokes = new LinkedHashMap<>();
    private LinkedHashMap<String, String> proverbs = new LinkedHashMap<>();

    JokeWorker(Socket s, ModeChanger mode, LinkedHashMap<String, String> jokes, LinkedHashMap<String, String> proverbs, ClientData clientData, NetworkData networkData) {
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
            setJokeOrProverb(currentIndex, mode.GetMode(), inObject, clientData);
            inObject.clientId = clientData.getId();
            objectOutputStream.writeObject(inObject); // Send the data back to client
            socket.close();

        } catch (IOException inpIoException) {
            System.out.println("Server error.");
            inpIoException.printStackTrace();
        }
    }

    void setJokeOrProverb(int index, int mode, NetworkData inObject, ClientData clientData) {
        if (mode == 0) {
            List<String> jokeKeys = new ArrayList<>(jokes.keySet());
            String key = jokeKeys.get(index);
            inObject.jokeOrProverbSentBack = key + " " + inObject.userName + ": " + jokes.get(key);
            clientData.addClientJokes(inObject.jokeOrProverbSentBack);
        } else {
            List<String> proverbKeys = new ArrayList<>(proverbs.keySet());
            String key = proverbKeys.get(index);
            inObject.jokeOrProverbSentBack = key + " " + inObject.userName + ": " + proverbs.get(key);
            clientData.addClientProverb(inObject.jokeOrProverbSentBack);
        }
        System.out.println(inObject.jokeOrProverbSentBack);
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

class ClientData {
    private UUID id;
    private String name;
    private boolean isJokeCycleCompleted;
    private boolean isProverbCycleCompleted;
    private int[] jokeOrder;
    private int[] proverbOrder;
    private List<String> clientJokes;
    private List<String> clientProverbs;

    public ClientData(UUID id, String name) {
        this.id = id;
        this.name = name;
        this.isJokeCycleCompleted = false;
        this.isProverbCycleCompleted = false;
        this.jokeOrder = new int[]{1, 2, 3, 4};
        this.proverbOrder = new int[]{1, 2, 3, 4};
        this.clientJokes = new ArrayList<>();
        this.clientProverbs = new ArrayList<>();
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

    public UUID getId() {
        return this.id;
    }

    public List<String> getClientJokes() {
        return this.clientJokes;
    }

    public List<String> getClientProverbs() {
        return this.clientProverbs;
    }

    public void addClientJokes(String joke) {
        this.clientJokes.add(joke);
    }

    public void addClientProverb(String proverb) {
        this.clientJokes.add(proverb);
    }
}


// AdminData class to maintain the mode
class AdminData implements Serializable {
    String mode;
}

// NetworkData class to maintain the necessary data between client and server
class NetworkData implements Serializable {
    UUID clientId;
    String userName;
    int jokeIndex;
    int proverbIndex;
    String jokeOrProverbSentBack;
    String messageToClient;
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