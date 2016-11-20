package com.buahbatu.jantung;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.text_user_name) TextInputLayout textUserName;
    @BindView(R.id.text_user_password) TextInputLayout textUserPass;

    @OnClick(R.id.button_login) void onLoginClick(){
        if (TextUtils.isEmpty(textUserName.getEditText().getText().toString())) {
            textUserPass.setError(null);
            textUserName.setError(getString(R.string.error_form));
        }else if (TextUtils.isEmpty(textUserPass.getEditText().getText().toString())){
            textUserName.setError(null);
            textUserPass.setError(getString(R.string.error_form));
        }else {
            textUserName.setError(null);
            textUserPass.setError(null);

            final ProgressDialog dialog = new ProgressDialog(LoginActivity.this,
                    AlertDialog.THEME_DEVICE_DEFAULT_DARK);
            dialog.setMessage("Logging in");
            dialog.show();

            AndroidNetworking.post(getString(R.string.base_url)+"/{user}" + getString(R.string.login_url))
                    .addPathParameter("user", "patient")
                    .addBodyParameter("username", textUserName.getEditText().getText().toString())
                    .addBodyParameter("password", textUserPass.getEditText().getText().toString())
                    .setPriority(Priority.MEDIUM).build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // do anything with response
                            dialog.dismiss();
                            Log.i("LOGIN", "onResponse: "+response.toString());
                            AppSetting.setLogin(LoginActivity.this, AppSetting.LOGGED_IN);
                            AppSetting.saveAccount(LoginActivity.this, textUserName.getEditText().getText().toString(),
                                    textUserPass.getEditText().getText().toString());
                            moveToHomeActivity();
                        }
                        @Override
                        public void onError(ANError error) {
                            // handle error
                            dialog.dismiss();
                            Log.i("LOGIN", "onError: "+ error.getErrorBody());
                            try {
                                JSONObject response = new JSONObject(error.getErrorBody());
                                if (response.getString("info").equals("username")){
                                    textUserName.setError("Username doesn't exist");
                                }else {
                                    textUserPass.setError("Wrong password");
                                }
                            }catch (JSONException ex){
                                ex.printStackTrace();
                            }
                        }
                    });
        }

    }

    @OnClick(R.id.button_register) void onRegisterClick(){
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }

    void moveToHomeActivity(){
        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AppSetting.isLoggedIn(LoginActivity.this)){
            setContentView(R.layout.activity_login);
            ButterKnife.bind(this);
            setTitle(getString(R.string.login));
        }else {
            moveToHomeActivity();
        }
    }
}
