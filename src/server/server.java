package server;


import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

// -Djava.security.policy==src/server/permissions.policy

public class server {

    public static void main(String [] args) {

        // Sets permission from policy file
        System.setSecurityManager(new SecurityManager());

        try {

            // Catalog will be a remote object
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

        while ( true );

    }

}
