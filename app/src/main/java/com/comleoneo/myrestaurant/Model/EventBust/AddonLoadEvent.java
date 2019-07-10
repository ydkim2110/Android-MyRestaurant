package com.comleoneo.myrestaurant.Model.EventBust;

import com.comleoneo.myrestaurant.Model.Addon;

import java.util.List;

public class AddonLoadEvent {
    private boolean success;
    private List<Addon> addonList;

    public AddonLoadEvent(boolean success, List<Addon> addonList) {
        this.success = success;
        this.addonList = addonList;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<Addon> getAddonList() {
        return addonList;
    }

    public void setAddonList(List<Addon> addonList) {
        this.addonList = addonList;
    }
}
