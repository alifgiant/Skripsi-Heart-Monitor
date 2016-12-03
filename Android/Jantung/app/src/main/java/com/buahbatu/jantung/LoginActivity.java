package com.buahbatu.jantung;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.LinearLayout;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
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

            JSONObject patient = new JSONObject();
            try {
                patient.put("username", textUserName.getEditText().getText().toString())
                        .put("password", textUserPass.getEditText().getText().toString());

                AppSetting.showProgressDialog(LoginActivity.this, "Logging in");

                AndroidNetworking.post(AppSetting.getHttpAddress(LoginActivity.this)
                        +"/{user}" + getString(R.string.login_url))
                        .addPathParameter("user", "patient")
                        .addJSONObjectBody(patient)
//                        .addBodyParameter("username", textUserName.getEditText().getText().toString())
//                        .addBodyParameter("password", textUserPass.getEditText().getText().toString())
                        .setPriority(Priority.MEDIUM).build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // do anything with response
                                AppSetting.dismissProgressDialog();
                                Log.i("LOGIN", "onResponse: "+response.toString());
                                AppSetting.setLogin(LoginActivity.this, AppSetting.LOGGED_IN);
                                AppSetting.saveAccount(LoginActivity.this, textUserName.getEditText().getText().toString(),
                                        textUserPass.getEditText().getText().toString());
                                moveToHomeActivity();
                            }
                            @Override
                            public void onError(ANError error) {
//                            // handle error
                                AppSetting.dismissProgressDialog();
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
                                // handle error
                                Log.i("LOGIN", "onError: "+ error.getErrorCode());
                                Log.i("LOGIN", "onError: "+ error.getErrorDetail());
                                Log.i("LOGIN", "onError: "+ error.getErrorBody());
                            }
                        });
            }catch (JSONException ex){
                ex.printStackTrace();
            }

        }

    }

    @OnClick(R.id.button_register) void onRegisterClick(){
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }

    @OnClick(R.id.logo_jantung) void setupIp(){
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle(getString(R.string.setup_ip));

        // Set up the input
        final TextInputEditText inputIp = new TextInputEditText(this);
        inputIp.setInputType(InputType.TYPE_CLASS_PHONE);
        inputIp.setHint(R.string.example_ip);
        final TextInputEditText inputPort = new TextInputEditText(this);
        inputPort.setInputType(InputType.TYPE_CLASS_PHONE);
        inputPort.setHint(R.string.example_port);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(inputIp);
        linearLayout.addView(inputPort);

        builder.setView(linearLayout);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // save a new ip and port
                if (!TextUtils.isEmpty(inputIp.getText().toString()) && !TextUtils.isEmpty(inputPort.getText().toString()) )
                    AppSetting.saveIp(LoginActivity.this, inputIp.getText().toString(), inputPort.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
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
