package shared;

import client.Product;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface CatalogRemote extends Remote {
    ArrayList<Category> getCategories       (                                              )    throws RemoteException;
    ArrayList<User>     getCategorySellers  ( int categoryId                               )    throws RemoteException;
    ResponseTypes       addCategoryRequest  ( ClientInterface ci , String categoryame      )    throws RemoteException;
    boolean             userExists          ( String email                                 )    throws RemoteException;
    User                authenticateUser    ( String ip , String email                     )    throws RemoteException;
    void                addCategorySeller   ( String email , String categoryName           )    throws RemoteException;
    ResponseTypes       updateSubscription  ( ClientInterface ci , String email            )    throws RemoteException;
    void                createUser          ( String email , String ip , int port ,
                                              ClientInterface clientInterface              )    throws RemoteException;

}
