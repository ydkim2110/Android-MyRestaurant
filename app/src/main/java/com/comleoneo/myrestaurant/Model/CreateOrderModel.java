package com.comleoneo.myrestaurant.Model;

import java.util.List;

public class CreateOrderModel {

    private boolean success;
    private List<CreateOrder> result;
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<CreateOrder> getResult() {
        return result;
    }

    public void setResult(List<CreateOrder> result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
