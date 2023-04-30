/* 2.0 2024-04-07 Saibaba Garbham, JokeServer.java, JokeClient.java and JokeClientAdmin.java
Copyright (c) 2023 by Saibaba Garbham with all rights reserved.

Name: Saibaba Garbham
Date: 2023-04-30
Java Version: 20 (build 20+36-2344)
Command-line compilation: > javac *.java [executed twice]

Running these programs:
We can run JokeServer, JokeClient and JokeClientAdmin on a same machine in multiple processes on separate command terminal windows.
We can run JokeClient and JokeServer without the JokeClientAdmin being started.
JokeServer will start on 4545 port by default. We can send an argument "secondary" which will make the JokeServer to run on 4546 port.
JokeClient will connect to 4545 port to the Server by default. We can pass upto two arguments(IPAddr) to Client, so that it will connect to each Server in the IPAddress.
JokeClientAdmin will connect to 5050 port to the Server by default. We can pass upto two arguments(IPAddr) to Client, so that it will connect to each Server in the IPAddress.

We can do the above process on different machines across the internet.

To run on localhost:

Command Window 1> java JokeServer
Command Window 2> java JokeClient
Command Window 2> java JokeClientAdmin
Command Window 3> java JokeClient
[...]
Command Window N> java JokeClient

Alternatively, to run over the Internet:

Command Window 1> java JokeServer server
Command Window 2> java JokeClient localhost 10.0.0.115 (Need to enter the actual IP address of JokeServer)
[...]

Files needed: JokeServer.java

Notes:

We build a client server program in which the JokeServer will give jokes/proverbs to the client. JokeClientAdmin can change the mode of the Server in between Joke/Proverb.
We used TCP/IP protocol to communicate between clients and servers.
Our JokeClient run simultaneously on port 4545 and take the user name. By pressing enter it can open a connection to the server and get a joke or proverb as per the server mode.
Jokeserver uses JokeWorker and handles the request from JokeClient and sends back a random joke or proverb back to client.
JokeServer can have multiple clients and the JokeWorker handles the requests from multiple clients.
JOkeServer uses AdminServer to expect the client admins and can change the mode of the server accordingly.

JokeServer:
    Our JokeServer waits for the JokeClient and JokeClientAdmin to send a request.
    When a JokeClient sends a request, based upon the mode, it will return a joke/proverb.
    When a JokeClientAdmin sends a request, it will change the mode in between Joke and Proverb.
    If we pass an argument of "secondary" to the server, then it will run the server in second port i.e., 4546.

JokeClient:
    JokeClient class will take the user name as input and asks the user to press "Enter" to get a joke or proverb.
    JokeClient expects two arguments which is Server one and Server two. If the user enters "s" then it will change the server.
    It will loop continuously until the user enters "quit".
    Displays the Joke or Proverb along with the user name to the user.

JokeClientAdmin:
    JokeClientAdmin class will asks the user to press "Enter" to change the mode of the server.
    By default it will talk to the default server i.e., running on port 5050.
    JokeClientAdmin expects two arguments which is Server one and Server two. If the user enters "s" then it will change the server and update the mode accordingly.
    It will loop continuously until the user enters "quit".
    Displays the current mode to the user.

--------------------

*/

import java.io.*;
import java.net.*;
import java.util.*;

class JokeClient {
    private static String userName;
    private UUID clientId; // To handle the clientId, I used UUID
    private int s1JokeIndex; // Server 1 Joke Index to get a joke
    private int s2JokeIndex; // Server 2 Joke Index to get a joke
    private int s1ProverbIndex; // Server 1 Proverb Index
    private int s2ProverbIndex; // Server 2 Proverb Index
    private String jokeOrProverbFromServer; // Joke Or Proverb message from Server

    public static void main(String argv[]) {
        // Creating jokeClient object here
        JokeClient jokeClient = new JokeClient(argv);
        jokeClient.run(argv); // Calling the run method
    }

    public JokeClient(String args[]) {
        // JokeClient Constructor
    }

    public void run(String args[]) {
        /* Variable declaration */
        String firstServername = "";
        String secondServername = "";
        String mainServername = "";
        int firstPort = 0;
        int secondPort = 0;
        int mainPort = 0;
        boolean isSecondServer = false;
        boolean isSecondServerExists = false;
        String userInput;

        // Setting the servername from args
        if (args.length < 1) { // If no params has passed
            firstServername = "localhost";
            firstPort = 4545;
        } else if (args.length == 1) { // If only a single ip address has given
            firstServername = args[0];
            firstPort = 4545;
        } else if (args.length == 2) { // If a secondary ip address has given then setting the ports and server names
            firstServername = args[0];
            firstPort = 4545;
            secondPort = 4546;
            secondServername = args[1];
            isSecondServerExists = true;
        }
        /* Server related messages displaying here */
        System.out.print("Server one: " + firstServername + ", port " + firstPort);
        if (isSecondServerExists) {
            System.out.println(" Server two: " + secondServername + ", port " + secondPort);
        }
        System.out.println();
        Scanner consoleIn = new Scanner(System.in);
        // Asks the user for their name
        System.out.print("Enter your name: ");
        System.out.flush();
        userName = consoleIn.nextLine();
        System.out.println("Hi " + userName);
        setDefaultValues(); // Setting the default values for the first time
        do {
            // Inorder to get a joke or proverb based on the joke server mode asking the user to press Enter.
            System.out.print("In order to receive a joke or proverb from server please press \"Enter\", or you can type \"s\" to switch between the servers, or type quit to terminate: ");
            userInput = consoleIn.nextLine();
            if (userInput != null) {
                if (userInput.indexOf("s") >= 0) { // If user enters change server
                    if (isSecondServerExists) { // Switching to second server
                        isSecondServer = !isSecondServer;
                        String displayMessage = String.format("Now communicating with: " + (isSecondServer ? (secondServername + ", port " + secondPort) : (firstServername + ", port " + firstPort)));
                        System.out.println(displayMessage);
                    } else {
                        System.out.println("No secondary server being used");
                    }
                } else if (userInput.indexOf("quit") < 0) { // Client pressed Enter
                    /* Setting the main server and main port based on server condition */
                    if (!isSecondServer) {
                        mainServername = firstServername;
                        mainPort = firstPort;
                    } else {
                        mainServername = secondServername;
                        mainPort = secondPort;
                    }
                    /* Connecting to server and getting Joke or Proverb here */
                    getJokeOrProverb(userName, mainServername, mainPort, isSecondServer);
                }
            }
        } while (userInput.indexOf("quit") < 0); // Look for joke or proverb until user enters quit
        System.out.println("Cancelled by user request. Thank you!");
    }

    /* To set default values for a client on first visit */
    void setDefaultValues() {
        clientId = UUID.randomUUID();
        s1JokeIndex = 0;
        s1ProverbIndex = 0;
        s2JokeIndex = 0;
        s2ProverbIndex = 0;
    }

    /* Connect to Server and get Joke or Proverb */
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
            NetworkData inNetworkObject = (NetworkData) objectInputStream.readObject(); // Reading the input stream from joke server

            /* Assigning the joke or proverb and indexes of joke/proverb to state variables */
            jokeOrProverbFromServer = inNetworkObject.jokeOrProverbSentBack;
            if (!isSecondServer) {
                s1JokeIndex = inNetworkObject.jokeIndex;
                s1ProverbIndex = inNetworkObject.proverbIndex;
            } else {
                s2JokeIndex = inNetworkObject.jokeIndex;
                s2ProverbIndex = inNetworkObject.proverbIndex;
            }

            // Displaying joke/proverb information here
            System.out.println(inNetworkObject.jokeOrProverbSentBack);
            // Displaying the cycle ends message below
            if (!inNetworkObject.cycleCompletedMessage.isEmpty()) {
                System.out.println(inNetworkObject.cycleCompletedMessage);
            }
            socket.close(); // Closing the socket connection
        } catch (ConnectException connectException) {
            System.out.println("\nFailed to connect to server. It seems the JokeServer is not running.\n");
            connectException.printStackTrace();
        } catch (UnknownHostException unknownHostException) {
            System.out.println("\nHost unknown exception.\n");
            unknownHostException.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (ClassNotFoundException classNotFoundException) {
            classNotFoundException.printStackTrace();
        }
    }
}

/* JokeClientAdmin class */
class JokeClientAdmin {
    private String currentMode; // Current mode variable

    public static void main(String argv[]) {
        JokeClientAdmin jokeClientAdmin = new JokeClientAdmin(argv);
        jokeClientAdmin.run(argv); // Starting here
    }

    /* Constructor here */
    public JokeClientAdmin(String args[]) {
    }

    public void run(String args[]) {
        /* Variable declaration goes here */
        String firstServername = "";
        String secondServername = "";
        String mainServername = "";
        int firstPort = 0;
        int secondPort = 0;
        int mainPort = 0;
        boolean isSecondServer = false;
        boolean isSecondServerExists = false;
        String userInput;

        // Setting the servername from args
        if (args.length < 1) { // If no args passed then localhost as default
            firstServername = "localhost";
            firstPort = 5050;
        } else if (args.length == 1) { // If a single argument is passed
            firstServername = args[0];
            firstPort = 5050;
        } else if (args.length == 2) { // If we pass two arguments then setting the ports and server names accordingly
            firstServername = args[0];
            firstPort = 5050;
            secondServername = args[1];
            secondPort = 5051;
            isSecondServerExists = true;
        }
        /* Server related information displaying here */
        System.out.print("Server one: " + firstServername + ", port " + firstPort);
        if (isSecondServerExists) {
            System.out.println(" Server two: " + secondServername + ", port " + secondPort);
        }
        System.out.println();
        // Take inputs from user
        System.out.println("The initial Joke Server mode is \"Joke\"");

        Scanner consoleIn = new Scanner(System.in);
        do {
            System.out.print("Press \"Enter\" to change Server mode, or \"s\" to change the server, or quit to end: ");
            userInput = consoleIn.nextLine();
            if (userInput != null) {
                if (userInput.indexOf("s") >= 0) { // Check if user trying to change server
                    if (isSecondServerExists) { // Changing server based on conditions
                        isSecondServer = !isSecondServer;
                        String displayMessage = String.format("Now communicating with: " + (isSecondServer ? (secondServername + ", port " + secondPort) : (firstServername + ", port " + firstPort)));
                        System.out.println(displayMessage);
                    } else {
                        System.out.println("No secondary server being used");
                    }
                } else if (userInput.indexOf("quit") < 0) { // Admin Client pressed Enter
                    /* Setting the main server based on requested server */
                    if (!isSecondServer) {
                        mainServername = firstServername;
                        mainPort = firstPort;
                    } else {
                        mainServername = secondServername;
                        mainPort = secondPort;
                    }
                    /* Connect to server */
                    connectToServer(mainServername, mainPort);
                }
            }
        } while (userInput.indexOf("quit") < 0); // Taking the joke client admin input until the admin enters quit
        System.out.println("Server connection was cancelled by user.");
    }

    /* Connect to Server */
    void connectToServer(String serverName, int mainPort) {
        try {
            /* Establishing Socket connection here */
            Socket socket = new Socket(serverName, mainPort);
            InputStream inStream = socket.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inStream);
            AdminData inObject = (AdminData) objectInputStream.readObject();
            currentMode = inObject.mode; // Set current mode
            System.out.println(serverName + " mode changed to : " + inObject.mode); // Mode changed message
            socket.close();
        } catch (ConnectException connectException) {
            System.out.println("\nFailed to establish a connection. It seems the JokeServer is not running.\n");
            connectException.printStackTrace();
        } catch (UnknownHostException unknownHostException) {
            System.out.println("\nHost unknown exception occurred.\n");
            unknownHostException.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (ClassNotFoundException classNotFoundException) {
            classNotFoundException.printStackTrace();
        }
    }
}

/* Joke Server class */
public class JokeServer {
    /* Creating two HashMap's for each type here. This is to handle initial jokes/proverbs and randomized ones */
    private static LinkedHashMap<String, String> jokes = new LinkedHashMap<>();
    private static LinkedHashMap<String, String> proverbs = new LinkedHashMap<>();
    private static LinkedHashMap<String, String> initJokes = new LinkedHashMap<>();
    private static LinkedHashMap<String, String> initProverbs = new LinkedHashMap<>();
    private static List<ClientData> clients = new ArrayList<>(); // Clients data
    private static int maxJokesOrProverbs = 4; // Max jokes length variable
    private static boolean isSecondServer = false; // IsSecondServer flag

    public static void main(String[] args) throws Exception {
        int queueLen = 6; // Simultaneous requests queue length
        int serverPort = 4545; // default port

        if (args.length == 1 && args[0].toLowerCase().equals("secondary")) { // Checking for secondary argument
            serverPort = 4546;
            isSecondServer = true;
        }
        Socket socket;
        System.out.println("Saibaba Garbham's Joke Server 1.0, listening for Joke Client at port  " + serverPort + ".\n");
        if(isSecondServer) {
            System.out.println("This is secondary server");
        }
        LoadInitJokesAndProverbs(); // Load initial jokes and proverbs
        ServerSocket serverSocket = new ServerSocket(serverPort, queueLen);
        System.out.println("ServerSocket is waiting for clients to connect..."); // Waiting for the client to connect


        ClientAdminManager clientAdminManager = new ClientAdminManager(isSecondServer);
        Thread clientAdminThread = new Thread(clientAdminManager);
        clientAdminThread.start();

        while (true) {
            socket = serverSocket.accept(); // Accept the client connection
            System.out.println("Connection from " + socket); // Connection info here
            InputStream socketInputStream = socket.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(socketInputStream);
            NetworkData inObject = (NetworkData) objectInputStream.readObject(); // Serialization goes here
            System.out.println("UserName: " + inObject.userName);
            ClientData clientData = null;
            boolean isClientFound = false;

            // Loop over the clients and assign if the client exists
            for (ClientData client :
                    clients) {
                if (inObject.clientId.equals(client.getId())) {
                    clientData = client;
                    isClientFound = true;
                }
            }
            /* If client exists then assign the pre-existing client data */
            if (!isClientFound) {
                clientData = new ClientData(inObject.clientId, inObject.userName);
                clients.add(clientData);
            }
            /* Check for cycle completed or not. If yes then randomize the jokes/proverbs */
            if (clientData.getIsJokeCycleCompleted()) {
                randomizeJokes(clientData);
            }
            if (clientData.getIsProverbCycleCompleted()) {
                randomizeProverbs(clientData);
            }
            /* Load jokes and proverbs as per randomization */
            LoadJokesAndProverbs(clientData);
            /* Start the joke worker */
            new JokeWorker(socket, clientAdminManager.mode, jokes, proverbs, clientData, inObject, isSecondServer).start();
        }
    }

    /* This method will loads the initial jokes and proverbs */
    public static void LoadInitJokesAndProverbs() {
        initJokes.put("JA", "School is a place where parents pay and children play.");
        initJokes.put("JB", "Boss is a person who arrives early when you are late and late when you arrive early.");
        initJokes.put("JC", "Criminal is a person no different from all except that he/she got caught.");
        initJokes.put("JD", "Doctor is a person who holds your ills by pills, and kills you by bills.");
        initProverbs.put("PA", "Empty wallet taught us lessons that no teacher can teach.");
        initProverbs.put("PB", "Being honest is the great policy.");
        initProverbs.put("PC", "Don't ever bite the hand that feeds you.");
        initProverbs.put("PD", "My hands are tied.");
    }

    /* This method will randomize the jokes using Collections.shuffle() */
    public static void randomizeJokes(ClientData clientData) {
        List<Integer> shuffledArray = new ArrayList<>();
        for (int ind = 1; ind <= maxJokesOrProverbs; ind++) {
            shuffledArray.add(ind);
        }
        Collections.shuffle(shuffledArray);
        clientData.setOrderedJokes(shuffledArray.stream().mapToInt(each -> each).toArray());
    }

    /* This method will randomize the proverbs using Collections.shuffle() */
    public static void randomizeProverbs(ClientData clientData) {
        List<Integer> shuffledArray = new ArrayList<>();
        for (int ind = 1; ind <= maxJokesOrProverbs; ind++) {
            shuffledArray.add(ind);
        }
        Collections.shuffle(shuffledArray);
        clientData.setOrderedProverbs(shuffledArray.stream().mapToInt(each -> each).toArray());
    }

    /* This method will load jokes and proverbs as per the joke/proverb order of randomization */
    public static void LoadJokesAndProverbs(ClientData clientData) {
        jokes = new LinkedHashMap<>();
        proverbs = new LinkedHashMap<>();
        int[] jokeOrder = clientData.getOrderedJokes();
        int[] proverbOrder = clientData.getOrderedProverbs();
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

/* Joke Worker class */
class JokeWorker extends Thread {
    Socket socket;
    ModeChanger mode;
    private int limiter;
    private int currentIndex;
    private NetworkData inObject;
    private ClientData clientData;
    private boolean isSecondServer;

    /* Variables to store jokes and proverbs */
    private LinkedHashMap<String, String> jokesHashMap = new LinkedHashMap<>();
    private LinkedHashMap<String, String> proverbsHashMap = new LinkedHashMap<>();

    /* JokeWorker constructor to assign all the required variables */
    JokeWorker(Socket s, ModeChanger mode, LinkedHashMap<String, String> jokesHashMap, LinkedHashMap<String, String> proverbsHashMap, ClientData clientData, NetworkData networkData, boolean isSecondServer) {
        this.socket = s;
        this.mode = mode;
        this.jokesHashMap = jokesHashMap;
        this.proverbsHashMap = proverbsHashMap;
        this.limiter = 4;
        this.inObject = networkData;
        this.clientData = clientData;
        this.isSecondServer = isSecondServer;
    }

    public void run() {
        try {
            OutputStream socketOutputStream = socket.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socketOutputStream);
            /* Set the joke/proverb cycle to false on initial client connect */
            clientData.setJokeCycleCompleted(false);
            clientData.setProverbCycleCompleted(false);
            inObject.cycleCompletedMessage = "";
            /* If mode is 0 i.e., Joke */
            if (this.mode.GetJokeServerMode() == 0) {
                this.currentIndex = inObject.jokeIndex; // Getting the joke index
                inObject.jokeIndex++;
                if (inObject.jokeIndex == this.limiter) { // Checking for cycle to be completed
                    inObject.jokeIndex = 0;
                    inObject.cycleCompletedMessage = "JOKE CYCLE COMPLETED";
                    clientData.setJokeCycleCompleted(true);
                }
            } else { // If mode is 1 i.e., Proverb
                this.currentIndex = inObject.proverbIndex; // Getting the proverb index
                inObject.proverbIndex++;
                if (inObject.proverbIndex == this.limiter) { // Checking for cycle to be completed
                    inObject.proverbIndex = 0;
                    inObject.cycleCompletedMessage = "PROVERB CYCLE COMPLETED";
                    clientData.setProverbCycleCompleted(true);
                }
            }
            /* Set the joke or proverb */
            setJokeOrProverb(currentIndex, mode.GetJokeServerMode(), inObject, clientData, isSecondServer);
            inObject.clientId = clientData.getId();
            objectOutputStream.writeObject(inObject); // Send the network data to client.
            socket.close();
            if(clientData.getIsProverbCycleCompleted()) {
                System.out.println(clientData.getName() + "'s PROVERB CYCLE COMPLETED");
            }
            if(clientData.getIsJokeCycleCompleted()) {
                System.out.println(clientData.getName() + "'s JOKE CYCLE COMPLETED");
            }
        } catch (IOException inpIoException) {
            System.out.println("JokeServer error occurred.");
            inpIoException.printStackTrace();
        }
    }

    /* This method will set the joke or proverb according to the mode and server */
    void setJokeOrProverb(int index, int mode, NetworkData inObject, ClientData clientData, boolean isSecondServer) {
        if (mode == 0) {
            List<String> jokeKeys = new ArrayList<>(jokesHashMap.keySet());
            String key = jokeKeys.get(index);
            inObject.jokeOrProverbSentBack = key + " " + inObject.userName + ": " + jokesHashMap.get(key);
            clientData.addClientJokes(inObject.jokeOrProverbSentBack);
        } else {
            List<String> proverbKeys = new ArrayList<>(proverbsHashMap.keySet());
            String key = proverbKeys.get(index);
            inObject.jokeOrProverbSentBack = key + " " + inObject.userName + ": " + proverbsHashMap.get(key);
            clientData.addClientProverb(inObject.jokeOrProverbSentBack);
        }
        System.out.println(inObject.jokeOrProverbSentBack);
        inObject.jokeOrProverbSentBack = isSecondServer ? "<S2> " + inObject.jokeOrProverbSentBack : inObject.jokeOrProverbSentBack;
    }
}

/* Admin Server class */
class ClientAdminManager implements Runnable {
    public static boolean adminModeChangerSwitch = true;
    ModeChanger mode = new ModeChanger(); // Handling mode changer in AdminWorker
    private boolean isSecondServer; // Second server related info

    ClientAdminManager(boolean isSecondServerExists) {
        this.isSecondServer = isSecondServerExists;
    }

    public void run() {

        int queueLength = 6;
        int clientAdminPort = 5050;
        if (this.isSecondServer) {
            clientAdminPort = 5051;
        }
        Socket clientAdminSocket;

        try {
            ServerSocket clientAdminServerSocket = new ServerSocket(clientAdminPort, queueLength);
            while (adminModeChangerSwitch) {
                // Accept the next admin client connection
                clientAdminSocket = clientAdminServerSocket.accept();
                new JokeClientAdminWorker(clientAdminSocket, mode).start();
            }
        } catch (IOException ioException) {
            System.out.println(ioException);
        }
    }
}
/* JokeClientAdminWorker class */
class JokeClientAdminWorker extends Thread { // Class definition.
    Socket adminWorkerSocket;
    ModeChanger mode;

    JokeClientAdminWorker(Socket s, ModeChanger mode) {
        this.adminWorkerSocket = s;
        this.mode = mode;
    }

    public void run() {
        try {
            AdminData inObject = new AdminData();

            OutputStream adminWorkerSocketOutputStream = adminWorkerSocket.getOutputStream();
            ObjectOutputStream adminWorkerObjectOutputStream = new ObjectOutputStream(adminWorkerSocketOutputStream);
            /* Changing the mode and setting joke/proverb accordingly*/
            mode.ChangeMode();
            if (mode.GetJokeServerMode() == 0) {
                inObject.mode = "Joke";
            } else {
                inObject.mode = "Proverb";
            }

            adminWorkerObjectOutputStream.writeObject(inObject); // Send the admin data back to client
            System.out.println("Changed the mode to : " + inObject.mode);
            adminWorkerSocket.close();

        } catch (IOException inpIoException) {
            System.out.println("Joke Server error.");
            inpIoException.printStackTrace();
        }
    }
}

/* ClientData model class */
class ClientData {
    private UUID id;
    private String name;
    private boolean isJokeCycleCompleted;
    private boolean isProverbCycleCompleted;
    private int[] orderedJokes;
    private int[] orderedProverbs;
    private List<String> clientJokes;
    private List<String> clientProverbs;

    public ClientData(UUID id, String name) {
        this.id = id;
        this.name = name;
        this.isJokeCycleCompleted = true;
        this.isProverbCycleCompleted = true;
        this.orderedJokes = new int[]{1, 2, 3, 4};
        this.orderedProverbs = new int[]{1, 2, 3, 4};
        this.clientJokes = new ArrayList<>();
        this.clientProverbs = new ArrayList<>();
    }

    /* Getters and Setters below */
    public int[] getOrderedJokes() {
        return orderedJokes;
    }

    public void setOrderedJokes(int[] orderedJokes) {
        this.orderedJokes = orderedJokes;
    }


    public int[] getOrderedProverbs() {
        return orderedProverbs;
    }

    public void setOrderedProverbs(int[] orderedProverbs) {
        this.orderedProverbs = orderedProverbs;
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

    /* Adding joke to the client */
    public void addClientJokes(String joke) {
        this.clientJokes.add(joke);
    }

    /* Adding proverb to the client */
    public void addClientProverb(String proverb) {
        this.clientProverbs.add(proverb);
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
    String cycleCompletedMessage;
}

/* Mode Changer class */
class ModeChanger {
    int jokeServerMode = 0;

    public int ChangeMode() {
        if (jokeServerMode == 0) {
            jokeServerMode = 1;
        } else {
            jokeServerMode = 0;
        }
        return jokeServerMode;
    }

    public int GetJokeServerMode() {
        return jokeServerMode;
    }
}


/*

------------------------------------ OUTPUT ------------------------------------
Please note that in the below log file I am using **** message **** for annotation

> java JokeServer
Saibaba Garbham's Joke Server 1.0, listening for Joke Client at port  4545.

ServerSocket is waiting for clients to connect...
Connection from Socket[addr=/127.0.0.1,port=60015,localport=4545]
UserName: Jon
JA Jon: School is a place where parents pay and children play.
Connection from Socket[addr=/127.0.0.1,port=60017,localport=4545]
UserName: Jon
JC Jon: Criminal is a person no different from all except that he/she got caught.
Connection from Socket[addr=/127.0.0.1,port=60018,localport=4545]
UserName: Jon
JB Jon: Boss is a person who arrives early when you are late and late when you arrive early.
Connection from Socket[addr=/127.0.0.1,port=60019,localport=4545]
UserName: Jon
JD Jon: Doctor is a person who holds your ills by pills, and kills you by bills.
Jon's JOKE CYCLE COMPLETED
Connection from Socket[addr=/127.0.0.1,port=60020,localport=4545]
UserName: Jon
JC Jon: Criminal is a person no different from all except that he/she got caught.
Connection from Socket[addr=/127.0.0.1,port=60021,localport=4545]
UserName: Jon
JB Jon: Boss is a person who arrives early when you are late and late when you arrive early.
Connection from Socket[addr=/127.0.0.1,port=60022,localport=4545]
UserName: Jon
JD Jon: Doctor is a person who holds your ills by pills, and kills you by bills.
Connection from Socket[addr=/127.0.0.1,port=60024,localport=4545]
UserName: Jon
JA Jon: School is a place where parents pay and children play.
Jon's JOKE CYCLE COMPLETED
Changed the mode to : Proverb
Connection from Socket[addr=/127.0.0.1,port=60120,localport=4545]
UserName: Saibaba
PC Saibaba: Don't ever bite the hand that feeds you.
Connection from Socket[addr=/127.0.0.1,port=60123,localport=4545]
UserName: Saibaba
PB Saibaba: Being honest is the great policy.
Connection from Socket[addr=/127.0.0.1,port=60125,localport=4545]
UserName: Saibaba
PD Saibaba: My hands are tied.
Connection from Socket[addr=/127.0.0.1,port=60126,localport=4545]
UserName: Saibaba
PA Saibaba: Empty wallet taught us lessons that no teacher can teach.
Saibaba's PROVERB CYCLE COMPLETED
Connection from Socket[addr=/127.0.0.1,port=60128,localport=4545]
UserName: Saibaba
PB Saibaba: Being honest is the great policy.
Connection from Socket[addr=/127.0.0.1,port=60129,localport=4545]
UserName: Saibaba
PC Saibaba: Don't ever bite the hand that feeds you.
Connection from Socket[addr=/127.0.0.1,port=60130,localport=4545]
UserName: Saibaba
PA Saibaba: Empty wallet taught us lessons that no teacher can teach.
Connection from Socket[addr=/127.0.0.1,port=60131,localport=4545]
UserName: Saibaba
PD Saibaba: My hands are tied.
Saibaba's PROVERB CYCLE COMPLETED
Connection from Socket[addr=/127.0.0.1,port=60209,localport=4545]
UserName: Saibaba
PD Saibaba: My hands are tied.

**** Requesting 8 proverbs for this client ****
> java JokeClient localhost localhost
Server one: localhost, port 4545 Server two: localhost, port 4546

Enter your name: Saibaba
Hi Saibaba
In order to receive a joke or proverb from server please press "Enter", or you can type "s" to switch between the servers, or type quit to terminate:
PC Saibaba: Don't ever bite the hand that feeds you.
In order to receive a joke or proverb from server please press "Enter", or you can type "s" to switch between the servers, or type quit to terminate:
PB Saibaba: Being honest is the great policy.
In order to receive a joke or proverb from server please press "Enter", or you can type "s" to switch between the servers, or type quit to terminate:
PD Saibaba: My hands are tied.
In order to receive a joke or proverb from server please press "Enter", or you can type "s" to switch between the servers, or type quit to terminate:
PA Saibaba: Empty wallet taught us lessons that no teacher can teach.
PROVERB CYCLE COMPLETED
In order to receive a joke or proverb from server please press "Enter", or you can type "s" to switch between the servers, or type quit to terminate:
PB Saibaba: Being honest is the great policy.
In order to receive a joke or proverb from server please press "Enter", or you can type "s" to switch between the servers, or type quit to terminate:
PC Saibaba: Don't ever bite the hand that feeds you.
In order to receive a joke or proverb from server please press "Enter", or you can type "s" to switch between the servers, or type quit to terminate:
PA Saibaba: Empty wallet taught us lessons that no teacher can teach.
In order to receive a joke or proverb from server please press "Enter", or you can type "s" to switch between the servers, or type quit to terminate:
PD Saibaba: My hands are tied.
PROVERB CYCLE COMPLETED
In order to receive a joke or proverb from server please press "Enter", or you can type "s" to switch between the servers, or type quit to terminate:
PD Saibaba: My hands are tied.
In order to receive a joke or proverb from server please press "Enter", or you can type "s" to switch between the servers, or type quit to terminate: quit
Cancelled by user request. Thank you!

**** Requesting 8 jokes for this client and then connected to second server and requesting for jokes/proverbs ****
> java JokeClient localhost localhost
Server one: localhost, port 4545 Server two: localhost, port 4546

Enter your name: Jon
Hi Jon
In order to receive a joke or proverb from server please press "Enter", or you can type "s" to switch between the servers, or type quit to terminate:
JA Jon: School is a place where parents pay and children play.
In order to receive a joke or proverb from server please press "Enter", or you can type "s" to switch between the servers, or type quit to terminate:
JC Jon: Criminal is a person no different from all except that he/she got caught.
In order to receive a joke or proverb from server please press "Enter", or you can type "s" to switch between the servers, or type quit to terminate:
JB Jon: Boss is a person who arrives early when you are late and late when you arrive early.
In order to receive a joke or proverb from server please press "Enter", or you can type "s" to switch between the servers, or type quit to terminate:
JD Jon: Doctor is a person who holds your ills by pills, and kills you by bills.
JOKE CYCLE COMPLETED
In order to receive a joke or proverb from server please press "Enter", or you can type "s" to switch between the servers, or type quit to terminate:
JC Jon: Criminal is a person no different from all except that he/she got caught.
In order to receive a joke or proverb from server please press "Enter", or you can type "s" to switch between the servers, or type quit to terminate:
JB Jon: Boss is a person who arrives early when you are late and late when you arrive early.
In order to receive a joke or proverb from server please press "Enter", or you can type "s" to switch between the servers, or type quit to terminate:
JD Jon: Doctor is a person who holds your ills by pills, and kills you by bills.
In order to receive a joke or proverb from server please press "Enter", or you can type "s" to switch between the servers, or type quit to terminate:
JA Jon: School is a place where parents pay and children play.
JOKE CYCLE COMPLETED
In order to receive a joke or proverb from server please press "Enter", or you can type "s" to switch between the servers, or type quit to terminate: s
Now communicating with: localhost, port 4546
In order to receive a joke or proverb from server please press "Enter", or you can type "s" to switch between the servers, or type quit to terminate:
<S2> JC Jon: Criminal is a person no different from all except that he/she got caught.
In order to receive a joke or proverb from server please press "Enter", or you can type "s" to switch between the servers, or type quit to terminate:
<S2> JB Jon: Boss is a person who arrives early when you are late and late when you arrive early.
In order to receive a joke or proverb from server please press "Enter", or you can type "s" to switch between the servers, or type quit to terminate:
<S2> PB Jon: Being honest is the great policy.
In order to receive a joke or proverb from server please press "Enter", or you can type "s" to switch between the servers, or type quit to terminate:
<S2> PA Jon: Empty wallet taught us lessons that no teacher can teach.
In order to receive a joke or proverb from server please press "Enter", or you can type "s" to switch between the servers, or type quit to terminate:
<S2> PC Jon: Don't ever bite the hand that feeds you.
In order to receive a joke or proverb from server please press "Enter", or you can type "s" to switch between the servers, or type quit to terminate:
<S2> PD Jon: My hands are tied.
PROVERB CYCLE COMPLETED
In order to receive a joke or proverb from server please press "Enter", or you can type "s" to switch between the servers, or type quit to terminate:
<S2> PD Jon: My hands are tied.
In order to receive a joke or proverb from server please press "Enter", or you can type "s" to switch between the servers, or type quit to terminate: quit
Cancelled by user request. Thank you!

**** This is second JokeServer ****
> java JokeServer secondary
Saibaba Garbham's Joke Server 1.0, listening for Joke Client at port  4546.

This is secondary server
ServerSocket is waiting for clients to connect...
Connection from Socket[addr=/127.0.0.1,port=60205,localport=4546]
UserName: Jon
JC Jon: Criminal is a person no different from all except that he/she got caught.
Connection from Socket[addr=/127.0.0.1,port=60207,localport=4546]
UserName: Jon
JB Jon: Boss is a person who arrives early when you are late and late when you arrive early.
Changed the mode to : Proverb
Connection from Socket[addr=/127.0.0.1,port=60220,localport=4546]
UserName: Jon
PB Jon: Being honest is the great policy.
Connection from Socket[addr=/127.0.0.1,port=60222,localport=4546]
UserName: Jon
PA Jon: Empty wallet taught us lessons that no teacher can teach.
Connection from Socket[addr=/127.0.0.1,port=60223,localport=4546]
UserName: Jon
PC Jon: Don't ever bite the hand that feeds you.
Connection from Socket[addr=/127.0.0.1,port=60224,localport=4546]
UserName: Jon
PD Jon: My hands are tied.
Jon's PROVERB CYCLE COMPLETED
Connection from Socket[addr=/127.0.0.1,port=60227,localport=4546]
UserName: Jon
PD Jon: My hands are tied.

**** In the below ClientAdmin, changed the modes for both servers
> java JokeClientAdmin localhost localhost
Server one: localhost, port 5050 Server two: localhost, port 5051

The initial Joke Server mode is "Joke"
Press "Enter" to change Server mode, or "s" to change the server, or quit to end:
localhost mode changed to : Proverb
Press "Enter" to change Server mode, or "s" to change the server, or quit to end: s
Now communicating with: localhost, port 5051
Press "Enter" to change Server mode, or "s" to change the server, or quit to end:
localhost mode changed to : Proverb
Press "Enter" to change Server mode, or "s" to change the server, or quit to end: quit
Server connection was cancelled by user.

----------------------------------------------------------

MY D2L JokeServer DISCUSSION FORUM POSTINGS:

Posting 1: Subject: Should the "Cycle Completed" message appear Automatically?
My Comment:
As per my understanding, we should display the "Cycle Completed" message on the client after the joke or proverb is received by the client.
This is because if the server is not reachable or shut down then it doesn't make any sense to say that the cycle is completed even before talking with the server.
Hope this helps.

Posting 2: Subject: Regarding JokeServer.java.txt TII report
My Comment:
It seems the file extension is wrong. The file name should be "JokeServer.java.txt" and the file type should be a txt.

Posting 3: Subject: Tip for randomizing
My Comment:
Collections.shuffle() is a great way to shuffle a list of values.
Before I came to know about Collections.shuffle() I tried different methods using Random.nextInt(), ListIterator, etc., where we have to prepare a new list out of the original list.
*/