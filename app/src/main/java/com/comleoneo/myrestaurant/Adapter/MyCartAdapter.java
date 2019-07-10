package com.comleoneo.myrestaurant.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.comleoneo.myrestaurant.Database.CartDataSource;
import com.comleoneo.myrestaurant.Database.CartDatabase;
import com.comleoneo.myrestaurant.Database.CartItem;
import com.comleoneo.myrestaurant.Database.LocalCartDataSource;
import com.comleoneo.myrestaurant.Interface.IOnImageViewAdapterClickListener;
import com.comleoneo.myrestaurant.Model.EventBust.CalculatePriceEvent;
import com.comleoneo.myrestaurant.R;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MyCartAdapter  extends RecyclerView.Adapter<MyCartAdapter.MyViewHolder> {

    private Context mContext;
    private List<CartItem> mCartItemList;
    private CartDataSource mCartDataSource;

    public MyCartAdapter(Context context, List<CartItem> cartItemList) {
        mContext = context;
        mCartItemList = cartItemList;
        mCartDataSource = new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_cart, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Picasso.get().load(mCartItemList.get(position).getFoodImage()).into(holder.img_food);
        holder.txt_food_name.setText(mCartItemList.get(position).getFoodName());
        holder.txt_food_price.setText(String.valueOf(mCartItemList.get(position).getFoodPrice()));
        holder.txt_quantity.setText(String.valueOf(mCartItemList.get(position).getFoodQuantity()));

        Double finalResult = mCartItemList.get(position).getFoodPrice()*mCartItemList.get(position).getFoodQuantity();
        holder.txt_price_new.setText(String.valueOf(finalResult));

        holder.txt_extra_price.setText(new StringBuilder("Extra Price($) : +")
        .append(mCartItemList.get(position).getFoodExtraPrice()));

        // Event
        holder.setIOnImageViewAdapterClickListener(new IOnImageViewAdapterClickListener() {
            @Override
            public void onCalculatePriceListener(View view, int position, boolean isDecrease, boolean isDelete) {
                // If not button delete food from Cart click
                if (!isDelete) {
                    // If decrease quantity
                    if (isDecrease) {
                        if (mCartItemList.get(position).getFoodQuantity() > 1) {
                            mCartItemList.get(position).setFoodQuantity(mCartItemList.get(position).getFoodQuantity()-1);
                        }
                    }
                    // If increase quantity
                    else {
                        if (mCartItemList.get(position).getFoodQuantity() < 99) {
                            mCartItemList.get(position).setFoodQuantity(mCartItemList.get(position).getFoodQuantity()+1);
                        }
                    }

                    // Update Cart
                    mCartDataSource.updateCart(mCartItemList.get(position))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<Integer>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onSuccess(Integer integer) {
                                    holder.txt_quantity.setText(String.valueOf(mCartItemList.get(position).getFoodQuantity()));
                                    EventBus.getDefault().postSticky(new CalculatePriceEvent());
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Toast.makeText(mContext, "[UPDATE CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                // Delete item
                else {
                    mCartDataSource.deleteCart(mCartItemList.get(position))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<Integer>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onSuccess(Integer integer) {
                                    notifyItemRemoved(position);
                                    EventBus.getDefault().postSticky(new CalculatePriceEvent());
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Toast.makeText(mContext, "[DELETE CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCartItemList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.txt_price_new)
        TextView txt_price_new;
        @BindView(R.id.txt_food_name)
        TextView txt_food_name;
        @BindView(R.id.txt_food_price)
        TextView txt_food_price;
        @BindView(R.id.txt_quantity)
        TextView txt_quantity;
        @BindView(R.id.txt_extra_price)
        TextView txt_extra_price;

        @BindView(R.id.img_food)
        ImageView img_food;
        @BindView(R.id.img_delete_food)
        ImageView img_delete_food;
        @BindView(R.id.img_decrease)
        ImageView img_decrease;
        @BindView(R.id.img_increase)
        ImageView img_increase;

        IOnImageViewAdapterClickListener mIOnImageViewAdapterClickListener;

        public void setIOnImageViewAdapterClickListener(IOnImageViewAdapterClickListener IOnImageViewAdapterClickListener) {
            mIOnImageViewAdapterClickListener = IOnImageViewAdapterClickListener;
        }

        Unbinder mUnbinder;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            mUnbinder = ButterKnife.bind(this, itemView);

            img_decrease.setOnClickListener(this);
            img_increase.setOnClickListener(this);
            img_delete_food.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == img_decrease) {
                mIOnImageViewAdapterClickListener.onCalculatePriceListener(v, getAdapterPosition(), true, false);
            }
            else if (v == img_increase) {
                mIOnImageViewAdapterClickListener.onCalculatePriceListener(v, getAdapterPosition(), false, false);
            }
            else if (v == img_delete_food) {
                mIOnImageViewAdapterClickListener.onCalculatePriceListener(v, getAdapterPosition(), true, true);
            }
        }
    }

}
