package shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInterface extends Remote {

    void notifyClient   ( String notification ) throws RemoteException;
    String getEmail     (                     ) throws RemoteException;

}
