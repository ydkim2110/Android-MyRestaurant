package com.comleoneo.myrestaurant.Model;

public class Size {
    private int id;
    private String description;
    private Float extraPrice;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Float getExtraPrice() {
        return extraPrice;
    }

    public void setExtraPrice(Float extraPrice) {
        this.extraPrice = extraPrice;
    }
}
