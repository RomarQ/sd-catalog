package client;

import shared.ClientInterface;

import java.io.Serializable;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import static shared.helper.getIp;

public class ClientInfo extends UnicastRemoteObject implements ClientInterface, Serializable {

    private ArrayList<Product> products = new ArrayList<>();
    private int currentProductId = 1;

    private String  email;
    private String  phone;

    private int     port;
    private String  ip;

    public ClientInfo( String email , String phone , int port ) throws RemoteException, SocketException {
        super();

        this.email  = email;
        this.phone  = phone;
        this.port   = port;
        this.ip     = getIp();
    }

    public ArrayList<Product> getProducts() {

        ArrayList<Product> clone = new ArrayList<>();

        for ( Product p : products )
            clone.add( new Product( p.getId() , p.getName() , p.getCategory() , p.getPrice() ) );

        return clone;

    }

    public int generateProductId() {
        return currentProductId++;
    }

    public int getCurrentProductId() {
        return currentProductId;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getClientIp() { return ip; }

    public int getPort() {
        return port;
    }

    public Product getProduct( String productName , String category ) {

        for ( Product p : products )
            if ( p.getName().equalsIgnoreCase(productName) && p.getCategory().equalsIgnoreCase(category) )
                return new Product( p.getId() , p.getName() , p.getCategory() , p.getPrice() );

        return null;
    }

    public void setIp() throws SocketException {
        this.ip = getIp();
    }


    public void addProduct( Product product ) {
        products.add( product );
    }

    public void updateProductPrice( String productName, String category , double price ) {

        for ( Product p : products )
            if ( p.getName().equalsIgnoreCase(productName) && p.getCategory().equalsIgnoreCase(category) ) {
                p.setPrice(price);
                break;
            }
    }

    public boolean productExist( String productName, String category ) {

        for ( Product p : products )
            if ( p.getName().equalsIgnoreCase(productName) && p.getCategory().equalsIgnoreCase(category) )
                return true;

        return false;

    }

    public void removeProductById( int Id ) {

        Product productToRemove = null;

        for ( Product p : products )
            if ( p.getId() == Id ) {
                productToRemove = p;
                break;
            }

        if ( productToRemove != null )
            products.remove(productToRemove);
    }

    /**
     * Remote Method that will be used to notify clients every time a category that they requested is available.
     */
    public void notifyClient( String notification ) {

        System.out.print("\n\n" + notification + "\n\n");

    }

}
