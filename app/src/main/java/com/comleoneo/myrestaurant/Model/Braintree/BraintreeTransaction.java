package com.comleoneo.myrestaurant.Model.Braintree;

public class BraintreeTransaction {
    private boolean success;
    private Transaction transaction;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}
