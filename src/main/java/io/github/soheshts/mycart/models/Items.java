package io.github.soheshts.mycart.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Items {
    List<Item> items = new ArrayList<Item>();

    public List getItems() {
        return items;
    }

    public void setItems(List items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "Items{" +
                "items=" + items +
                '}';
    }
}
