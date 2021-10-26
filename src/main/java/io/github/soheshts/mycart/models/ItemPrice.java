package io.github.soheshts.mycart.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemPrice {
    private Integer basePrice;
    private Integer sellingPrice;

    public Integer getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Integer basePrice) {
        this.basePrice = basePrice;
    }

    public Integer getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(Integer sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    @Override
    public String toString() {
        return "ItemPrice{" +
                "basePrice=" + basePrice +
                ", sellingPrice=" + sellingPrice +
                '}';
    }
}
