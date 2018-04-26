package server;

import shared.ClientInterface;

import java.io.Serializable;

public class CategoryRequest implements Serializable {

    private String                  categoryName;
    private ClientInterface clientInterface;

    public CategoryRequest ( String categoryName, ClientInterface clientInterface) {
        this.categoryName = categoryName;
        this.clientInterface = clientInterface;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public ClientInterface getClientInterface() {
        return clientInterface;
    }
}
