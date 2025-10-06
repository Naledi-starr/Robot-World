package za.co.wethinkcode.client.connection;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Handles the client's network connection to the server.
 * Provides methods to connect, send/receive messages, check connection status, and disconnect.
 */

public class ClientConnection {
    private Socket socket;
    private PrintWriter out;
    private Scanner in;

    /**
     * Creates a new uninitialized client connection.
     */

    public ClientConnection() {
    }

    /**
     * Creates a client connection with a predefined socket, input, and output stream.
     * Intended for testing.
     *
     * @param socket the socket to use
     * @param out the output writer
     * @param in the input scanner
     */

    public ClientConnection(Socket socket, PrintWriter out, Scanner in) {
        this.socket = socket;
        this.out = out;
        this.in = in;
    }

    /**
     * Connects to a server using the given IP address and port.
     *
     * @param ipAddress the server's IP address
     * @param port the server's port
     * @return {@code true} if connection is successful, otherwise {@code false}
     */

    public boolean connect(String ipAddress, int port) {
        try {
            System.out.println("Attempting to connect to " + ipAddress + ":" + port);
            socket = new Socket();

            // Set a timeout (2 seconds)
            socket.connect(new InetSocketAddress(ipAddress, port), 2000); // Timeout in milliseconds

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new Scanner(socket.getInputStream());

            System.out.println("✅ Connected to the server at " + ipAddress + ":" + port);
            return true;
        } catch (UnknownHostException e) {
            System.out.println("❌ Unknown host: " + ipAddress);
        } catch (SocketTimeoutException e) {
            System.out.println("❌ Connection timed out for " + ipAddress + ":" + port);
        } catch (IOException e) {
            System.out.println("❌ IO Error when connecting to " + ipAddress + ":" + port + ": " + e.getMessage());
        } catch (SecurityException e) {
            System.out.println("❌ Security violation when connecting: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Invalid port number: " + port);
        }
        return false;
    }

    // Package-private methods for testability
    Socket createSocket(String ipAddress, int port) throws IOException {
        return new Socket(ipAddress, port);
    }

    PrintWriter createPrintWriter(OutputStream outputStream) {
        return new PrintWriter(outputStream, true);
    }

    /**
     * Returns {@code true} if the client is currently connected.
     *
     * @return connection status
     */

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    /**
     * Sends a message to the server.
     *
     * @param message the message to send
     */

    public void send(String message) {
        if (out != null) {
            out.println(message);
        } else {
            System.out.println("❌ Cannot send, connection not established.");
        }
    }

    /**
     * Receives a message from the server.
     *
     * @return the received message, or {@code null} if unavailable
     */

    public String receive() {
        if (in != null && in.hasNextLine()) {
            return in.nextLine();
        }
        return null;
    }

    /**
     * Disconnects from the server and closes the connection.
     */

    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
                System.out.println("Disconnected from the server.");
            }
        } catch (IOException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }
}