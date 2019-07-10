package com.comleoneo.myrestaurant.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.comleoneo.myrestaurant.Common.Common;
import com.comleoneo.myrestaurant.Interface.IOnRecyclerViewClickListener;
import com.comleoneo.myrestaurant.MenuActivity;
import com.comleoneo.myrestaurant.Model.EventBust.MenuItemEvent;
import com.comleoneo.myrestaurant.Model.Restaurant;
import com.comleoneo.myrestaurant.R;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyRestaurantAdapter extends RecyclerView.Adapter<MyRestaurantAdapter.MyViewHolder> {

    private Context mContext;
    private List<Restaurant> mRestaurantList;

    public MyRestaurantAdapter(Context context, List<Restaurant> restaurantList) {
        mContext = context;
        mRestaurantList = restaurantList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_restaurant, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Picasso.get().load(mRestaurantList.get(position).getImage()).into(holder.img_restaurant);
        holder.txt_restaurant_name.setText(new StringBuilder(mRestaurantList.get(position).getName()));
        holder.txt_restaurant_name.setText(new StringBuilder(mRestaurantList.get(position).getAddress()));

        // Remember implement it if you don't want to get  crash
        holder.setIOnRecyclerViewClickListener((view, i) -> {
            Common.currentRestaurant = mRestaurantList.get(i);
            // Here use postSticky, that mean this event will be listen from other activity
            // It will different with just 'post'
            EventBus.getDefault().postSticky(new MenuItemEvent(true, mRestaurantList.get(i)));
            mContext.startActivity(new Intent(mContext, MenuActivity.class));
        });
    }

    @Override
    public int getItemCount() {
        return mRestaurantList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.txt_restaurant_name)
        TextView txt_restaurant_name;
        @BindView(R.id.txt_restaurant_address)
        TextView txt_restaurant_address;
        @BindView(R.id.img_restaurant)
        ImageView img_restaurant;

        IOnRecyclerViewClickListener mIOnRecyclerViewClickListener;

        public void setIOnRecyclerViewClickListener(IOnRecyclerViewClickListener IOnRecyclerViewClickListener) {
            mIOnRecyclerViewClickListener = IOnRecyclerViewClickListener;
        }

        Unbinder mUnbinder;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            mUnbinder = ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mIOnRecyclerViewClickListener.onClick(v, getAdapterPosition());
        }
    }
}
