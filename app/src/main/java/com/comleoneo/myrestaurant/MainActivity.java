package com.comleoneo.myrestaurant;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.comleoneo.myrestaurant.Common.Common;
import com.comleoneo.myrestaurant.Model.TokenModel;
import com.comleoneo.myrestaurant.Retrofit.IMyRestaurantAPI;
import com.comleoneo.myrestaurant.Retrofit.RetrofitClient;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int APP_REQUEST_CODE = 1234;

    private IMyRestaurantAPI mIMyRestaurantAPI;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private AlertDialog mDialog;


    @BindView(R.id.btn_sign_in)
    Button btn_sign_in;

    @OnClick(R.id.btn_sign_in)
    void loginUser() {
        Log.d(TAG, "loginUser: called!!");
        Intent intent = new Intent(this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder builder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE,
                        AccountKitActivity.ResponseType.TOKEN);
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                builder.build());
        startActivityForResult(intent, APP_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE) {
            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            String toastMessage;
            if (loginResult.getError() != null) {
                toastMessage = loginResult.getError().getErrorType().getMessage();
                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
            } else if (loginResult.wasCancelled()) {
                toastMessage = "Login Cancelled";
                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
            } else {
                // Login Success
                mDialog.show();

                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {

                        // Save FBID
                        Paper.book().write(Common.REMEMBER_FBID, account.getId());

                        FirebaseInstanceId.getInstance()
                                .getInstanceId()
                                .addOnFailureListener(e -> {
                                    Toast.makeText(MainActivity.this, "[GET TOKEN]", Toast.LENGTH_SHORT).show();
                                })
                                .addOnCompleteListener(task -> {
                                    mCompositeDisposable.add(mIMyRestaurantAPI.updateTokenToServer(Common.API_KEY,
                                            account.getId(), task.getResult().getToken())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(tokenModel -> {

                                        if (!tokenModel.isSuccess()) {
                                            Toast.makeText(MainActivity.this, "[UPDATE TOKEN]"+tokenModel.getMessage(), Toast.LENGTH_SHORT).show();
                                        }

                                        mCompositeDisposable.add(mIMyRestaurantAPI.getUser(Common.API_KEY, account.getId())
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(userModel -> {

                                                    // If user already in database
                                                    if (userModel.isSuccess()) {
                                                        Common.currentUser = userModel.getResult().get(0);
                                                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                                        finish();
                                                    }
                                                    // If user not register
                                                    else {
                                                        startActivity(new Intent(MainActivity.this, UpdateInfoActivity.class));
                                                        finish();
                                                    }

                                                    mDialog.dismiss();

                                                }, throwable -> {
                                                    mDialog.dismiss();
                                                    Toast.makeText(MainActivity.this, "[GET USER]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                    Log.d(TAG, "[GET USER]: " + throwable.getMessage());
                                                }));

                                    }, throwable -> {
                                        Toast.makeText(MainActivity.this, "[UPDATE TOKEN ERROR]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();

                                    }));
                                });
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {
                        Toast.makeText(MainActivity.this, "[ACCOUNT KIT ERROR]" + accountKitError.getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        mCompositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: started!!");

        ButterKnife.bind(this);

        init();
    }

    private void init() {
        Log.d(TAG, "init: called!!");
        Paper.init(this);
        mDialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        mIMyRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT)
                .create(IMyRestaurantAPI.class);
    }


}
