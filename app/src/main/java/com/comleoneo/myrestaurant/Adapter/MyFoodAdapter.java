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
import com.comleoneo.myrestaurant.Database.CartDataSource;
import com.comleoneo.myrestaurant.Database.CartDatabase;
import com.comleoneo.myrestaurant.Database.CartItem;
import com.comleoneo.myrestaurant.Database.LocalCartDataSource;
import com.comleoneo.myrestaurant.FoodDetailActivity;
import com.comleoneo.myrestaurant.Interface.IFoodDetailOrCartClickListener;
import com.comleoneo.myrestaurant.Model.EventBust.FoodDetailEvent;
import com.comleoneo.myrestaurant.Model.FavoriteModel;
import com.comleoneo.myrestaurant.Model.FavoriteOnlyId;
import com.comleoneo.myrestaurant.Model.Food;
import com.comleoneo.myrestaurant.R;
import com.comleoneo.myrestaurant.Retrofit.IMyRestaurantAPI;
import com.comleoneo.myrestaurant.Retrofit.RetrofitClient;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MyFoodAdapter extends RecyclerView.Adapter<MyFoodAdapter.MyViewHolder> {

    private Context mContext;
    private List<Food> mFoodList;
    private CompositeDisposable mCompositeDisposable;
    private CartDataSource mCartDataSource;
    private IMyRestaurantAPI mIMyRestaurantAPI;

    public void onStop() {
        mCompositeDisposable.clear();
    }

    public MyFoodAdapter(Context context, List<Food> foodList) {
        mContext = context;
        mFoodList = foodList;
        mCompositeDisposable = new CompositeDisposable();
        mCartDataSource = new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());
        mIMyRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_food, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Picasso.get().load(mFoodList.get(position).getImage())
                .placeholder(R.drawable.app_icon).into(holder.img_food);

        holder.txt_food_name.setText(mFoodList.get(position).getName());
        holder.txt_food_price.setText(new StringBuilder(mContext.getString(R.string.money_sign))
                .append(mFoodList.get(position).getPrice()));

        // Check Favorite
        if (Common.currentFavOfRestaurant != null && Common.currentFavOfRestaurant.size() > 0) {
            if (Common.checkFavorite(mFoodList.get(position).getId())) {
                holder.img_fav.setImageResource(R.drawable.ic_favorite_button_color_24dp);
                holder.img_fav.setTag(true);
            }
            else {
                holder.img_fav.setImageResource(R.drawable.ic_favorite_border_button_color_24dp);
                holder.img_fav.setTag(false);
            }
        }
        else {
            // Default, all item is no favorite
            holder.img_fav.setTag(false);
        }

        // Event
        holder.img_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView fav = (ImageView)v;

                if ((Boolean)fav.getTag()) {
                    // If tag = true -> Favorite item clicked
                    mCompositeDisposable.add(mIMyRestaurantAPI.removeFavorite(Common.API_KEY,
                            Common.currentUser.getFbid(),
                            mFoodList.get(position).getId(),
                            Common.currentRestaurant.getId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(favoriteModel -> {

                        if (favoriteModel.isSuccess() && favoriteModel.getMessage().contains("Success")) {
                            fav.setImageResource(R.drawable.ic_favorite_border_button_color_24dp);
                            fav.setTag(false);
                            if (Common.currentFavOfRestaurant != null) {
                                Common.removeFavorite(mFoodList.get(position).getId());
                            }
                        }

                    }, throwable -> {
                        Toast.makeText(mContext, "[REMOVE FAV]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }));
                }
                else {
                    mCompositeDisposable.add(mIMyRestaurantAPI.insertFavorite(Common.API_KEY,
                            Common.currentUser.getFbid(),
                            mFoodList.get(position).getId(),
                            Common.currentRestaurant.getId(),
                            Common.currentRestaurant.getName(),
                            mFoodList.get(position).getName(),
                            mFoodList.get(position).getImage(),
                            mFoodList.get(position).getPrice())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(favoriteModel -> {

                                if (favoriteModel.isSuccess() && favoriteModel.getMessage().contains("Success")) {
                                    fav.setImageResource(R.drawable.ic_favorite_button_color_24dp);
                                    fav.setTag(true);
                                    if (Common.currentFavOfRestaurant != null) {
                                        Common.currentFavOfRestaurant.add(new FavoriteOnlyId(mFoodList.get(position).getId()));
                                    }
                                }

                            }, throwable -> {
                                Toast.makeText(mContext, "[ADD FAV]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }));
                }
            }
        });

        holder.setIFoodDetailOrCartClickListener((view, i, isDetail) -> {
            if (isDetail) {

                mContext.startActivity(new Intent(mContext, FoodDetailActivity.class));
                EventBus.getDefault().postSticky(new FoodDetailEvent(true, mFoodList.get(i)));

            } else {
                // Cart create
                CartItem cartItem = new CartItem();
                cartItem.setFoodId(mFoodList.get(i).getId());
                cartItem.setFoodName(mFoodList.get(i).getName());
                cartItem.setFoodPrice(mFoodList.get(i).getPrice());
                cartItem.setFoodImage(mFoodList.get(i).getImage());
                cartItem.setFoodQuantity(1);
                cartItem.setUserPhone(Common.currentUser.getUserPhone());
                cartItem.setRestaurantId(Common.currentRestaurant.getId());
                cartItem.setFoodAddon("NORMAL");
                cartItem.setFoodSize("NORMAL");
                cartItem.setFoodExtraPrice(0.0);
                cartItem.setFbid(Common.currentUser.getFbid());

                mCompositeDisposable.add(mCartDataSource.insertOrReplaceAll(cartItem)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Toast.makeText(mContext, "Added to Cart", Toast.LENGTH_SHORT).show();
                }, throwable -> {
                    
                }));
            }
        });

    }

    @Override
    public int getItemCount() {
        return mFoodList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.img_food)
        ImageView img_food;
        @BindView(R.id.img_fav)
        ImageView img_fav;
        @BindView(R.id.txt_food_name)
        TextView txt_food_name;
        @BindView(R.id.txt_food_price)
        TextView txt_food_price;
        @BindView(R.id.img_detail)
        ImageView img_detail;
        @BindView(R.id.img_cart)
        ImageView img_add_cart;

        IFoodDetailOrCartClickListener mIFoodDetailOrCartClickListener;

        public void setIFoodDetailOrCartClickListener(IFoodDetailOrCartClickListener IFoodDetailOrCartClickListener) {
            mIFoodDetailOrCartClickListener = IFoodDetailOrCartClickListener;
        }

        Unbinder mUnbinder;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            mUnbinder = ButterKnife.bind(this, itemView);

            img_detail.setOnClickListener(this);
            img_add_cart.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.img_detail) {
                mIFoodDetailOrCartClickListener.onFoodItemClickListener(v, getAdapterPosition(), true);
            }
            else if (v.getId() == R.id.img_cart) {
                mIFoodDetailOrCartClickListener.onFoodItemClickListener(v, getAdapterPosition(), false);
            }
        }
    }
}
