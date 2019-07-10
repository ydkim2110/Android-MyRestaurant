package com.comleoneo.myrestaurant.Model;

public class User {

    private String fbid;
    private String userPhone;
    private String name;
    private String address;

    public User(String fbid, String userPhone, String name, String address) {
        this.fbid = fbid;
        this.userPhone = userPhone;
        this.name = name;
        this.address = address;
    }

    public String getFbid() {
        return fbid;
    }

    public void setFbid(String fbid) {
        this.fbid = fbid;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
