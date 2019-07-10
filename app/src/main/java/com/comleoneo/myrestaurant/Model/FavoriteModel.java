package com.comleoneo.myrestaurant.Model;

import java.util.List;

public class FavoriteModel {

    private boolean success;
    private String message;
    private List<Favorite> result;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Favorite> getResult() {
        return result;
    }

    public void setResult(List<Favorite> result) {
        this.result = result;
    }
}
