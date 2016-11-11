package com.buahbatu.jantung;

import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.text_user_name) TextInputLayout textUserName;
    @BindView(R.id.text_user_password) TextInputLayout textUserPass;

    @OnClick(R.id.button_login) void onLoginClick(){
        if (TextUtils.isEmpty(textUserName.getEditText().getText().toString())) {
            textUserPass.setError(null);
            textUserName.setError("Please fill this field");
        }else if (TextUtils.isEmpty(textUserPass.getEditText().getText().toString())){
            textUserName.setError(null);
            textUserPass.setError("Please fill this field");
        }else {
            textUserName.setError(null);
            textUserPass.setError(null);
            AndroidNetworking.post(getString(R.string.base_url) + getString(R.string.login))
                    .addBodyParameter(getString(R.string.username), textUserName.getEditText().getText().toString())
                    .addBodyParameter(getString(R.string.password), textUserPass.getEditText().getText().toString())
                    .setPriority(Priority.MEDIUM).build()
                    .getAsString(new StringRequestListener() { // get json array later
                        @Override
                        public void onResponse(String response) {
                            // later move to loading activity
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        }

                        @Override
                        public void onError(ANError anError) {
                            // handle error
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        }
                    });
        }

    }

    @OnClick(R.id.button_register) void onRegisterClick(){
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        setTitle(getString(R.string.login));

        AndroidNetworking.initialize(this);
    }
}
