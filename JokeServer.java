/* 2.0 2024-04-07 Saibaba Garbham, ColorServer.java and ColorClient.java
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

Files needed: ColorServer.java, ColorClient.java

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

Thanks:

https://www.comrevo.com/2019/07/Sending-objects-over-sockets-Java-example-How-to-send-serialized-object-over-network-in-Java.html (Code dated 2019-07-09, by Ramesh)
https://rollbar.com/blog/java-socketexception/#
Also: Hughes, Shoffner and Winslow for Inet code.

--------------------

*/

import java.io.*;
import java.net.*;
import java.util.*;

class JokeClient {
    private static String userName;
    private int jokeIndex;
    private int proverbIndex;
    private String jokeOrProverbFromServer;

    public static void main(String argv[]) {
        // Creating JokeClient object here
        JokeClient jokeClient = new JokeClient(argv);
        jokeClient.run(argv);
    }

    public JokeClient(String args[]) {
        System.out.println("ColorClient constructor");
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
        System.out.print("Enter your name: ");
        System.out.flush();
        userName = consoleIn.nextLine();
        jokeIndex = 0;
        proverbIndex = 0;
        System.out.println("Hi " + userName);
        String userInput;
        do {
            System.out.print("Press \"Enter\" to get a joke or proverb, or quit to end: ");
            userInput = consoleIn.nextLine();
            if (userInput != null) {
                if (userInput.indexOf("quit") < 0) { // Client pressed Enter
                    getJokeOrProverb(userName, servername);
                }
            }
        } while (userInput.indexOf("quit") < 0); // Asks user for a color until user enters quit
        System.out.println("Cancelled by user request.");
    }

    void getJokeOrProverb(String userName, String serverName) {
        try {
            // Setting all the colordata here
            NetworkData networkData = new NetworkData();
            networkData.userName = userName;
            networkData.jokeIndex = jokeIndex;
            networkData.proverbIndex = proverbIndex;

            // Creating a socket connection below
            // Socket socket = new Socket("UNKNOWNHOST", 45565); // Demonstrate the UH exception below.
            int primaryPort = 4545;
            Socket socket = new Socket(serverName, primaryPort);
            System.out.println("\nWe have successfully connected to the ColorServer at port " + primaryPort);

            OutputStream outputStream = socket.getOutputStream(); // Get output stream from socket
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream); // Serializing the object here

            objectOutputStream.writeObject(networkData); // Pass the serialized object to the network
            System.out.println("We have send the serialized values to the JokeServer's server socket");

            InputStream inStream = socket.getInputStream(); // Get input stream from server
            ObjectInputStream objectInputStream = new ObjectInputStream(inStream);
            NetworkData inObject = (NetworkData) objectInputStream.readObject(); // Reading the input stream from server

            // Assigning the count and colors to state variables
            jokeOrProverbFromServer = inObject.jokeOrProverbSentBack;
            jokeIndex = inObject.jokeIndex;
            proverbIndex = inObject.proverbIndex;

            // Displaying all the information here
            System.out.println("\nFROM THE SERVER:");
            System.out.println("The joke sent back is: " + inObject.jokeOrProverbSentBack);
            if (!inObject.messageToClient.isEmpty()) {
                System.out.println("Message : " + inObject.messageToClient);
            }
            System.out.println("Closing the connection to the server.\n");
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
        // Creating colorClient object here
        JokeClientAdmin jokeClientAdmin = new JokeClientAdmin(argv);
        jokeClientAdmin.run(argv);
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
        System.out.print("### Hello Joke Client Admin ###");
        String userInput;
        do {
            System.out.print("Press \"Enter\" to change Server mode, or quit to end: ");
            userInput = consoleIn.nextLine();
            if (userInput != null) {
                if (userInput.indexOf("quit") < 0) { // Client Admin pressed Enter
                    connectToServer(servername);
                }
            }
        } while (userInput.indexOf("quit") < 0); // Asks user for a color until user enters quit
        System.out.println("Cancelled by user request.");
    }

    void connectToServer(String serverName) {
        try {
            Socket socket = new Socket(serverName, 5050);
            System.out.println("\nWe have successfully connected to the JokeServer at port 5050");

            InputStream inStream = socket.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inStream);
            AdminData inObject = (AdminData) objectInputStream.readObject();

            currentMode = inObject.mode;

            System.out.println("\nFROM THE SERVER:");
            System.out.println("Mode changed to : " + inObject.mode);
            System.out.println("Closing the connection to the server.\n");
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

class AdminData implements Serializable {
    String mode;
}

class NetworkData implements Serializable {
    String userName;
    int jokeIndex;
    int proverbIndex;
    String jokeOrProverbSentBack;
    String messageToClient;
}

class JokeWorker extends Thread { // Class definition. Extending Thread because these worker threads may run simultaneously
    Socket socket; // Class member - socket
    ToggleMode mode;
    private static Dictionary<String, String> jokes = new Hashtable<>();
    private static Dictionary<String, String> proverbs = new Hashtable<>();
    private int limiter;
    private int currentIndex;

    JokeWorker(Socket s, ToggleMode mode) {
        this.socket = s;
        this.mode = mode;
        jokes.put("JA", "JOKE A");
        jokes.put("JB", "JOKE B");
        jokes.put("JC", "JOKE C");
        jokes.put("JD", "JOKE D");
        proverbs.put("PA", "PROVERB A");
        proverbs.put("PB", "PROVERB B");
        proverbs.put("PC", "PROVERB C");
        proverbs.put("PD", "PROVERB D");
        this.limiter = 4;
    }

    public void run() {
        try {
            InputStream inputStream = socket.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

            NetworkData inObject = (NetworkData) objectInputStream.readObject(); // Reading the colordata object from client

            OutputStream outputStream = socket.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

            System.out.println("\nFROM THE CLIENT:\n");
            System.out.println("Username: " + inObject.userName);

            if (this.mode.GetMode() == 0) {
                this.currentIndex = inObject.jokeIndex;
                inObject.jokeIndex++;
                if(this.currentIndex==4) {
                    this.currentIndex = 0;
                    inObject.jokeIndex = 0;
                }
            } else {
                this.currentIndex = inObject.proverbIndex;
                inObject.proverbIndex++;
                if(this.currentIndex==4) {
                    this.currentIndex = 0;
                    inObject.proverbIndex = 0;
                }
            }

            inObject.jokeOrProverbSentBack = getJokeOrProverb(currentIndex, mode.GetMode());
            if (currentIndex == 3) {
                inObject.messageToClient = mode.GetMode() == 0 ? "JOKE CYCLE COMPLETED" : "PROVERB CYCLE COMPLETED";
            } else {
                inObject.messageToClient = "";
            }
            objectOutputStream.writeObject(inObject); // Send the data back to client

            System.out.println("Closing the client socket connection...");
            socket.close();

        } catch (IOException inpIoException) {
            System.out.println("Server error.");
            inpIoException.printStackTrace();
        } catch (ClassNotFoundException classNotFoundException) {
            classNotFoundException.printStackTrace(); // This class is defined in server code
        }
    }

    String getJokeOrProverb(int index, int mode) {
        if (mode == 0) {
            if (index == 0) {
                return jokes.get("JA");
            } else if (index == 1) {
                return jokes.get("JB");
            } else if (index == 2) {
                return jokes.get("JC");
            } else if (index == 3) {
                return jokes.get("JD");
            }
        } else {
            if (index == 0) {
                return proverbs.get("PA");
            } else if (index == 1) {
                return proverbs.get("PB");
            } else if (index == 2) {
                return proverbs.get("PC");
            } else if (index == 3) {
                return proverbs.get("PD");
            }
        }
        return "";
    }
}

class ToggleMode {
    int Mode = 0;

    public int SetMode() {
        if (Mode == 0) {
            Mode = 1;
        } else {
            Mode = 0;
        }
        return (Mode);
    }

    public int GetMode() {
        return (Mode);
    }

}

class AdminLooper implements Runnable {
    public static boolean adminControlSwitch = true;
    ToggleMode mode = new ToggleMode();

    public void run() {
        System.out.println("In the admin looper thread");

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
    ToggleMode mode;

    JokeClientAdminWorker(Socket s, ToggleMode mode) {
        System.out.println("Is it reached here 2");
        this.socket = s;
        this.mode = mode;
    }

    public void run() {
        try {
            AdminData inObject = new AdminData();

            OutputStream outputStream = socket.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

            //System.out.println(mode.GetMode());
            mode.SetMode();
            if (mode.GetMode() == 0) {
                inObject.mode = "Joke";
            } else {
                inObject.mode = "Proverb";
            }

            objectOutputStream.writeObject(inObject); // Send the data back to client
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
    public static void main(String[] args) throws Exception {
        int queueLen = 6; // Number of simultaneous requests for Operating System to queue
        int serverPort = 4545;
        Socket socket;

        System.out.println("Saibaba Garbham's Joke Server 1.0 starting up, listening for Joke Client at port  " + serverPort + ".\n");

        // Listen for connections at port ServerPort. Doorbell socket
        ServerSocket serverSocket = new ServerSocket(serverPort, queueLen);
        System.out.println("ServerSocket awaiting connections..."); // Waiting for the client to ring the bell

        AdminLooper AL = new AdminLooper();
        Thread t = new Thread(AL);
        t.start();

        while (true) { // Use Ctrl C to manually terminate the server
            socket = serverSocket.accept(); // Answer the client connection
            System.out.println("Connection from " + socket);
            new JokeWorker(socket, AL.mode).start();
        }
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