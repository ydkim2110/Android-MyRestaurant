package com.comleoneo.myrestaurant.Model;

public class TokenModel {
    private boolean success;
    private String message;
    private MyRestaurantToken result;

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

    public MyRestaurantToken getResult() {
        return result;
    }

    public void setResult(MyRestaurantToken result) {
        this.result = result;
    }
}
