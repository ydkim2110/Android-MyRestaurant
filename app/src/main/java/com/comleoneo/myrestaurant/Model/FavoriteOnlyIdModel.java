package com.comleoneo.myrestaurant.Model;

import java.util.List;

public class FavoriteOnlyIdModel {
    private boolean success;
    private String message;
    private List<FavoriteOnlyId> result;

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

    public List<FavoriteOnlyId> getResult() {
        return result;
    }

    public void setResult(List<FavoriteOnlyId> result) {
        this.result = result;
    }
}
