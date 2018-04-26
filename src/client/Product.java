package client;

import java.io.Serializable;

public class Product implements Serializable {

    private int Id;
    private String name;
    private String category;
    private double price;

    public Product ( int Id , String name , String category , double price ) {

        this.Id         = Id;
        this.name       = name;
        this.category   = category;
        this.price      = price;

    }

    public int getId() { return Id; }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice( double price ) { this.price = price; }

}
