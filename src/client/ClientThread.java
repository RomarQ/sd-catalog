package client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ClientThread extends Thread {

    private ServerSocket server_socket;
    private int port;
    private boolean running = false;

    ClientThread( int port ) {
        super();

        this.port = port;
    }

    /**
     * Starts a server Thread for the client, so the client can receive direct connection from other clients
     */
    public void startClientServer() {
        try {
            this.server_socket = new ServerSocket( port );
            this.start();
        } catch (IOException e ) {
            System.err.println( "Failure: Socket couldn't OPEN! -> " + e );
        } finally {
            System.out.println( "Success: Server socket is now Listening on port " + port + "..." );
        }
    }

    /**
     * Stops the Thread
     */
    public void stopServer() {

        this.running = false;

        try {

            this.server_socket.close();
            this.interrupt();

        } catch (IOException e ) {
            System.err.println( "Failure: Server socket couldn't be CLOSED! -> " + e );
        }
    }


    @Override
    /**
     * Waits for connection requests from other clients and starts a connection Thread to interact with them.
     */
    public void run() {

        this.running = true;

        while( running ) {
            try {
                // Waits for connection requests from other clients
                Socket socket = server_socket.accept();

                // Received a connection request, so lets
                // start the connection between both clients
                Connection connection = new Connection( socket );
                connection.start();

            } catch ( SocketException e ) {
                System.out.println( "Success: ServerSocket is now CLOSED on port " + port + "..." );
            } catch ( IOException e ) {
                System.err.println( "Failure: Something happened while attempting to accept a new connection! -> ");
                e.printStackTrace();
            }

        }

    }

}
