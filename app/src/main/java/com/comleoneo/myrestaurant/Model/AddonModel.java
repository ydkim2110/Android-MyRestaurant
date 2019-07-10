package com.comleoneo.myrestaurant.Model;

import java.util.List;

public class AddonModel {
    private boolean success;
    private String message;
    private List<Addon> result;

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

    public List<Addon> getResult() {
        return result;
    }

    public void setResult(List<Addon> result) {
        this.result = result;
    }
}
