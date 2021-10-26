package io.github.soheshts.mycart.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StockDetails {
    private Integer availableStock;
    private String unitOfMeasure;
    private String soldOut;
    private String damaged;


    public Integer getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(Integer availableStock) {
        this.availableStock = availableStock;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public String getSoldOut() {
        return soldOut;
    }

    public void setSoldOut(String soldOut) {
        this.soldOut = soldOut;
    }

    public String getDamaged() {
        return damaged;
    }

    public void setDamaged(String damaged) {
        this.damaged = damaged;
    }

    @Override
    public String toString() {
        return "StockDetails{" +
                "availableStock=" + availableStock +
                ", unitOfMeasure='" + unitOfMeasure + '\'' +
                ", soldOut='" + soldOut + '\'' +
                ", damaged='" + damaged + '\'' +
                '}';
    }
}
