package com.comleoneo.myrestaurant.Adapter;

import com.comleoneo.myrestaurant.Model.Restaurant;

import java.util.List;

import ss.com.bannerslider.adapters.SliderAdapter;
import ss.com.bannerslider.viewholder.ImageSlideViewHolder;

public class RestaurantSliderAdapter extends SliderAdapter {

    private List<Restaurant> mRestaurantList;

    public RestaurantSliderAdapter(List<Restaurant> restaurantList) {
        this.mRestaurantList = restaurantList;
    }

    @Override
    public int getItemCount() {
        return mRestaurantList.size();
    }

    @Override
    public void onBindImageSlide(int position, ImageSlideViewHolder imageSlideViewHolder) {
        imageSlideViewHolder.bindImageSlide(mRestaurantList.get(position).getImage());
    }
}
