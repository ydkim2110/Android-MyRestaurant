package com.comleoneo.myrestaurant.Model;

import java.util.List;

public class SizeModel {

    private boolean success;
    private String message;
    private List<Size> result;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<Size> getResult() {
        return result;
    }

    public void setResult(List<Size> result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
