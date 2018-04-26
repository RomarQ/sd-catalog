package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerThread extends Thread {

    private ServerSocket server_socket;
    private int port;
    private boolean running = false;

    ServerThread( int port ) {
        super();

        this.port = port;
    }

    public void startServer() {

        try {
            this.server_socket = new ServerSocket( port );
            this.start();

        } catch (IOException e ) {
            System.err.println( "Failure: Server socket couldn't OPEN! -> " + e );
        } finally {
            System.out.println( "Success: Server socket is now Listening on port " + port + "..." );
        }

    }

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
    public void run() {

        this.running = true;

        try {
            Catalog catalog = new Catalog();
            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind("Catalog", catalog );

            System.out.println("# Catalog is now available for Remote Access!");

        } catch ( RemoteException e ) {
            e.printStackTrace();
        } catch ( AlreadyBoundException e ) {
            e.printStackTrace();
        }

        while( running ) {
            try
            {
                //Waits for a connection
                Socket socket = server_socket.accept();

                // Pass the socket to the RequestHandler thread for processing
                Connection connection = new Connection( socket );
                connection.start();

            }
            catch ( SocketException e ) {
                System.out.println( "Success: Server socket is now CLOSED on port " + port + "..." );
            }
            catch ( IOException e )
            {
                System.err.println( "Failure: Something happened while attempting to accept a new connection! -> ");
                e.printStackTrace();
            }
        }

    }

}
