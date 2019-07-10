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
import com.comleoneo.myrestaurant.FoodListActivity;
import com.comleoneo.myrestaurant.Interface.IOnRecyclerViewClickListener;
import com.comleoneo.myrestaurant.Model.Category;
import com.comleoneo.myrestaurant.Model.EventBust.FoodListEvent;
import com.comleoneo.myrestaurant.R;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyCategoryAdapter extends RecyclerView.Adapter<MyCategoryAdapter.MyViewHolder> {

    private Context mContext;
    private List<Category> mCategoryList;

    public MyCategoryAdapter(Context context, List<Category> categoryList) {
        mContext = context;
        mCategoryList = categoryList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_category, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Picasso.get().load(mCategoryList.get(position).getImage()).into(holder.img_restaurant);
        holder.txt_category.setText(mCategoryList.get(position).getName());

        holder.setIOnRecyclerViewClickListener((view, i) -> {

            // Send sticky post event to FoodListActivity
            EventBus.getDefault().postSticky(new FoodListEvent(true, mCategoryList.get(i)));
            mContext.startActivity(new Intent(mContext, FoodListActivity.class));

        });
    }

    @Override
    public int getItemCount() {
        return mCategoryList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.img_category)
        ImageView img_restaurant;
        @BindView(R.id.txt_category)
        TextView txt_category;

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

    @Override
    public int getItemViewType(int position) {
        if (mCategoryList.size() == 1) {
            return Common.DEFAULT_COLUMN_COUNT;
        }
        else {
            if (mCategoryList.size() % 2 == 0) {
                return Common.DEFAULT_COLUMN_COUNT;
            }
            else {
                return (position > 1 && position == mCategoryList.size()-1)
                        ? Common.FULL_WIDTH_COLUMN
                        : Common.DEFAULT_COLUMN_COUNT;
            }
        }
    }
}
