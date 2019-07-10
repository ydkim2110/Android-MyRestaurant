package com.comleoneo.myrestaurant.Model.EventBust;

import com.comleoneo.myrestaurant.Model.Size;

import java.util.List;

public class SizeLoadEvent {
    private boolean success;
    private List<Size> sizeList;

    public SizeLoadEvent(boolean success, List<Size> sizeList) {
        this.success = success;
        this.sizeList = sizeList;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<Size> getSizeList() {
        return sizeList;
    }

    public void setSizeList(List<Size> sizeList) {
        this.sizeList = sizeList;
    }
}
