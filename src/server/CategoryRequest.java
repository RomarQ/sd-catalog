package server;

import shared.ClientInterface;

import java.io.Serializable;

public class CategoryRequest implements Serializable {

    private String categoryName;
    private String clientEmail;

    public CategoryRequest ( String categoryName, String clientEmail ) {
        this.categoryName = categoryName;
        this.clientEmail = clientEmail;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getClientEmail() {
        return clientEmail;
    }
}
