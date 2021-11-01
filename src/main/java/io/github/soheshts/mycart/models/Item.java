package io.github.soheshts.mycart.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Item {
    private String _id;
    private String itemName;
    private String categoryId;
    private ItemPrice itemPrice;
    private StockDetails stockDetails;
    private Boolean specialProduct;
    private List<Review> review;

    public List<Review> getReview() {
        return review;
    }

    public void setReview(List<Review> review) {
        this.review = review;
    }

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

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
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
                ", categoryName='" + categoryId + '\'' +
                ", itemPrice=" + itemPrice +
                ", stockDetails=" + stockDetails +
                ", specialProduct=" + specialProduct +
                '}';
    }
}

