package shared;

import java.io.Serializable;

public class CategorySeller implements Serializable {

    private int sellerId;
    private int categoryId;

    public CategorySeller( int sellerId , int categoryId ) {
        this.sellerId   = sellerId;
        this.categoryId = categoryId;
    }

    public int getSellerId() {
        return sellerId;
    }

    public int getCategoryId() {
        return categoryId;
    }
}
