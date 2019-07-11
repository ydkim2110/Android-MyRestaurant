package com.comleoneo.myrestaurant;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.comleoneo.myrestaurant.Common.Common;
import com.comleoneo.myrestaurant.Retrofit.IMyRestaurantAPI;
import com.comleoneo.myrestaurant.Retrofit.RetrofitClient;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class UpdateInfoActivity extends AppCompatActivity {

    private static final String TAG = UpdateInfoActivity.class.getSimpleName();

    private IMyRestaurantAPI mIMyRestaurantAPI;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private AlertDialog mDialog;

    @BindView(R.id.edt_user_name)
    EditText edt_user_name;
    @BindView(R.id.edt_user_address)
    EditText edt_user_address;
    @BindView(R.id.btn_update)
    Button btn_update;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onDestroy() {
        mCompositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_info);
        Log.d(TAG, "onCreate: started!!");

        ButterKnife.bind(this);

        init();
        initView();
    }

    // Override back arrow
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        Log.d(TAG, "initView: called!!");
        toolbar.setTitle(getString(R.string.update_information));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: called!!");
                mDialog.show();
                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        Log.d(TAG, "onSuccess: called!!");
                        mCompositeDisposable.add(mIMyRestaurantAPI.updateUserInfo(Common.API_KEY,
                                account.getPhoneNumber().toString(),
                                edt_user_name.getText().toString(),
                                edt_user_address.getText().toString(),
                                account.getId())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(updateUserModel -> {

                                    if (updateUserModel.isSuccess()) {
                                        // If user has been update, just refresh again
                                        mCompositeDisposable.add(mIMyRestaurantAPI.getUser(Common.API_KEY, account.getId())
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(userModel -> {

                                                    if (userModel.isSuccess()) {
                                                        Common.currentUser = userModel.getResult().get(0);
                                                        startActivity(new Intent(UpdateInfoActivity.this, HomeActivity.class));
                                                        finish();
                                                    }
                                                    else {
                                                        Toast.makeText(UpdateInfoActivity.this, "[[GET USER RESULT]]"+userModel.getResult().get(0), Toast.LENGTH_SHORT).show();
                                                    }
                                                    mDialog.dismiss();

                                                }, throwable -> {
                                                    mDialog.dismiss();
                                                    Toast.makeText(UpdateInfoActivity.this, "[GET USER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                }));
                                    }
                                    else {
                                        mDialog.dismiss();
                                        Toast.makeText(UpdateInfoActivity.this, "[UPDATE USER API RETURN]" + updateUserModel.getMessage(), Toast.LENGTH_SHORT).show();
                                    }

                                }, throwable -> {
                                    mDialog.dismiss();
                                    Toast.makeText(UpdateInfoActivity.this, "[UPDATE USER API]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                }));
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {
                        Toast.makeText(UpdateInfoActivity.this, "[Account Kit Error]" + accountKitError.getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        if (Common.currentUser != null && !TextUtils.isEmpty(Common.currentUser.getName()))
            edt_user_name.setText(Common.currentUser.getName());
        if (Common.currentUser != null && !TextUtils.isEmpty(Common.currentUser.getAddress()))
            edt_user_address.setText(Common.currentUser.getAddress());
    }

    private void init() {
        Log.d(TAG, "init: called!!");
        mDialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        mIMyRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT)
                .create(IMyRestaurantAPI.class);
    }
}
