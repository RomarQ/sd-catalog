package server;


public class server {

    static int default_port = 3100;

    public static void main(String [] args) {


        /**
         * Tries to start the server socket on default_port
         * @server_socket instance of ServerSocket that will allow clients to connect to the server
         * @default_port  port where server socket will be listening to all connections
         */

        ServerThread server = new ServerThread( default_port );
        server.startServer();

        while ( true );

    }

}
