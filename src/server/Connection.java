package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Connection extends Thread {

    private Socket socket;

    Connection(Socket socket ) {
        super();

        this.socket = socket;

    }

    @Override
    public void run() {

        System.out.println( "Received a connection from "+ socket.getRemoteSocketAddress());

        try (
            // Get input and output streams
            ObjectOutputStream oos = new ObjectOutputStream( socket.getOutputStream() );
            ObjectInputStream  ois = new ObjectInputStream( socket.getInputStream() )
        ) {
            // Write out to the client
            oos.writeObject("Welcome dear Seller, you are now connected to the server!");
            oos.flush();

            String input = (String) ois.readObject();

            while (!input.equals("0")) {

                switch (input) {
                    case "1":
                        oos.writeObject(input);
                        oos.flush();
                        break;
                    default:
                        oos.writeObject("wrong");
                        oos.flush();
                        break;

                }

                input = (String) ois.readObject();

            }

            oos.writeObject("Ready to close!");
            oos.flush();


            System.out.println("Connection with " + socket.getRemoteSocketAddress() + " was terminated!");

            socket.close();

        } catch ( IOException e ) {
            e.printStackTrace();
        } catch ( ClassNotFoundException e ) {
            e.printStackTrace();
        }

    }
}
