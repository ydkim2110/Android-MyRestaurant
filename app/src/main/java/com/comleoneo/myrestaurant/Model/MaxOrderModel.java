package com.comleoneo.myrestaurant.Model;

import java.util.List;

public class MaxOrderModel {
    private boolean success;
    private String message;
    private List<MaxOrder> result;

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

    public List<MaxOrder> getResult() {
        return result;
    }

    public void setResult(List<MaxOrder> result) {
        this.result = result;
    }
}
