package com.motion.lab.pulse;

import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Bundle bundle = getIntent().getBundleExtra("extra");

        TextInputLayout idView = (TextInputLayout) findViewById(R.id.device_id);
        idView.getEditText().setText(bundle.getString("id"));
    }
}
