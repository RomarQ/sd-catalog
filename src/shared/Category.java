package shared;


import java.io.Serializable;

public class Category implements Serializable {

    private int id;
    private String category_name;

    public Category( int id , String category ) {
        this.id            = id;
        this.category_name = category;
    }

    public Category( Category category ) {
        this.id = category.getId();
        this.category_name = category.getCategoryName();
    }

    public int getId() {
        return id;
    }

    public String getCategoryName() {
        return category_name;
    }

}