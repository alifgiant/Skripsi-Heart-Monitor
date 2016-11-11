package com.buahbatu.jantung;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.buahbatu.jantung.model.ItemDevice;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

    @BindView(R.id.item_image) ImageView itemImage;
    @BindView(R.id.item_name) TextView itemName;
    @BindView(R.id.item_id) TextView itemId;
    @BindView(R.id.item_rate) TextView itemRate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        String name = getIntent().getStringExtra(getString(R.string.key_name));
        String id = getIntent().getStringExtra(getString(R.string.key_id));
        boolean isMale = getIntent().getBooleanExtra(getString(R.string.key_gender), false);
        int rate = getIntent().getIntExtra(getString(R.string.key_rate), 1000);
        int condition = getIntent().getIntExtra(getString(R.string.key_condition), 0);

        Log.i("DetailAct", "onCreate: "+rate);

        if (isMale){
            switch (condition){
                case 0: itemImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.boy0));
                    break;
                case 1: itemImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.boy1));
                    break;
                case 2: itemImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.boy2));
                    break;
            }

        }else {
            switch (condition){
                case 0: itemImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.girl0));
                    break;
                case 1: itemImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.girl1));
                    break;
                case 2: itemImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.boy2));
                    break;
            }
        }

        itemName.setText(name);
        itemId.setText(String.format(Locale.US, "%s: %s", getString(R.string.device_id),
                id));

        itemRate.setText(String.format(Locale.US, "%d", rate));
    }
}
