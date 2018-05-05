package client;

import shared.ResponseTypes;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static client.Client.clientInfo;

/**
 * Deals with all requests coming from other clients
 */
public class Connection extends Thread {

    ResponseTypes response;
    Socket socket;

    Connection ( Socket socket ) {
        super();
        this.socket = socket;
    }

    @Override
    public void run() {

        String clientAddress = socket.getRemoteSocketAddress().toString();
        System.out.print("\n\nClient " + clientAddress + " just connected with you!\n");

        try (
            ObjectInputStream   ois = new ObjectInputStream( socket.getInputStream() );
            ObjectOutputStream  oos = new ObjectOutputStream( socket.getOutputStream() )
        ) {
            // Telling to the other client that everything is ready
            oos.writeObject(ResponseTypes.CONNECTION_ESTABLISHED);

            response = (ResponseTypes) ois.readObject();

            while( !socket.isClosed() ) {

                switch (response) {

                    case PRODUCTS_REQUEST           :   String categoryName = (String) ois.readObject();
                                                        oos.writeObject(clientInfo.getProductsByCategoryName(categoryName));
                                                        oos.flush();
                                                        System.out.print("\nAsked for a list of your products.");
                                                        break;

                    case CONTACT_REQUEST            :   oos.writeObject("Email: " + clientInfo.getEmail() + ", Phone: " + clientInfo.getPhone() );
                                                        oos.flush();
                                                        System.out.print("\nRequested your contact.");
                                                        break;

                    case CATEGORY_REQUEST_UPDATE    :   System.out.print(ois.readObject());
                                                        break;

                    default                         :   oos.writeObject(ResponseTypes.UNKNOWN_REQUEST);
                }

                response = (ResponseTypes) ois.readObject();

            }

            socket.close();

        } catch ( EOFException e ) {
            System.out.println("\n\nClient " + clientAddress + " disconnected!\n\n");
        } catch ( IOException e ) {
            e.printStackTrace();
        } catch ( ClassNotFoundException e ) {
            e.printStackTrace();
        }

    }
}
