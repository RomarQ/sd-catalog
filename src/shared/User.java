package shared;

import java.io.Serializable;


public class User implements Serializable {

    private int     id;
    private String  email;
    private String  ip;
    private int     port;

    private ClientInterface clientInterface;

    public User( int id , String email , String ip , int port , ClientInterface ci ) {
        this.id              = id;
        this.ip              = ip;
        this.port            = port;
        this.email           = email;
        this.clientInterface = ci;
    }

    public User( User user ) {
        this.id              = user.getId();
        this.ip              = user.getIp();
        this.port            = user.getPort();
        this.email           = user.getEmail();
        this.clientInterface = user.getClientInterface();
    }

    public int getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getEmail() {
        return email;
    }

    public ClientInterface getClientInterface() { return clientInterface; }

    public void updateIp(String ip) {
        this.ip = ip;
    }

    public void updateClientInterface( ClientInterface ci ) {
        this.clientInterface = ci;
    }
}
