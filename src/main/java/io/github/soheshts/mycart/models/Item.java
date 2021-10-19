package io.github.soheshts.mycart.models;

public class Item {
    private String _id;
    private String itemName;
    private String categoryName;
    private ItemPrice itemPrice;
    private StockDetails stockDetails;
    private Boolean specialProduct;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public ItemPrice getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(ItemPrice itemPrice) {
        this.itemPrice = itemPrice;
    }

    public StockDetails getStockDetails() {
        return stockDetails;
    }

    public void setStockDetails(StockDetails stockDetails) {
        this.stockDetails = stockDetails;
    }

    public Boolean getSpecialProduct() {
        return specialProduct;
    }

    public void setSpecialProduct(Boolean specialProduct) {
        this.specialProduct = specialProduct;
    }

    @Override
    public String toString() {
        return "Item{" +
                "_id='" + _id + '\'' +
                ", itemName='" + itemName + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", itemPrice=" + itemPrice +
                ", stockDetails=" + stockDetails +
                ", specialProduct=" + specialProduct +
                '}';
    }
}

