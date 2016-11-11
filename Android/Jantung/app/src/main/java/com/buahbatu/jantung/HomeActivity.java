package com.buahbatu.jantung;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.buahbatu.jantung.model.ItemDevice;
import com.buahbatu.jantung.model.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeActivity extends AppCompatActivity{

    private static final int HEADER = 85;
    private static final int DEVICE = 629;
    private List<Item> items;

    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @OnClick(R.id.fab) void onFabClick(){
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle(getString(R.string.add_device));

        // Set up the input
        final TextInputEditText deviceId = new TextInputEditText(this);
        TextInputEditText devicePassword = new TextInputEditText(this);
        deviceId.setInputType(InputType.TYPE_CLASS_TEXT);
        deviceId.setHint(R.string.device_id);
        devicePassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        devicePassword.setHint(R.string.device_pass);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(deviceId);
        linearLayout.addView(devicePassword);

        builder.setView(linearLayout);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // nama asli, dan gender minta dari server
                items.add(new ItemDevice("Newly Added", DEVICE, deviceId.getText().toString(), true));
                recyclerView.scrollToPosition(items.size()-1);
                Toast.makeText(HomeActivity.this, "A family/friend added", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onResume() {
        super.onResume();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getItemType() == DEVICE){
                Log.i("HOME ACT", "onResume: "+i);
                ItemDevice device = (ItemDevice) items.get(i);
                device.setRateRandom();
                viewAdapter.notifyDataSetChanged();
            }
        }
    }

    ViewAdapter viewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        // init adapter and data holder
        viewAdapter = new ViewAdapter();
        items = new ArrayList<>();

        // dummy data
        items.add(new Item("My Device", HEADER));
        items.add(new ItemDevice("Alif Akbar", DEVICE, "H001", true));
        items.add(new Item("Friend Device", HEADER));
        items.add(new ItemDevice("Masyithah", DEVICE, "H002", false));
        items.add(new ItemDevice("Sarah", DEVICE, "H003", false));
        items.add(new ItemDevice("Fahmi", DEVICE, "H004", true));
        items.add(new ItemDevice("Rere", DEVICE, "H005", false));
        items.add(new ItemDevice("Nana", DEVICE, "H006", false));
        items.add(new ItemDevice("Dani", DEVICE, "H007", true));

        recyclerView.setLayoutManager(new LinearLayoutManager(HomeActivity.this));
        recyclerView.setAdapter(viewAdapter);
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ViewHolder(View itemView) {
            super(itemView);
        }
    }

    class ViewAdapter extends RecyclerView.Adapter<ViewHolder>{
        @Override
        public int getItemViewType(int position) {
            return items.get(position).getItemType();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            if (viewType == HEADER)
                view = LayoutInflater.from(HomeActivity.this).inflate(R.layout.item_header, parent, false);
            else
                view = LayoutInflater.from(HomeActivity.this).inflate(R.layout.item_device, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Item item = items.get(position);
            TextView textName = (TextView) holder.itemView.findViewById(R.id.item_name);
            textName.setText(item.getName());
            if (item.getItemType() == DEVICE){
                ItemDevice device = (ItemDevice)item;

                ImageView image = (ImageView) holder.itemView.findViewById(R.id.item_image);
                if (device.isMale()){
                    switch (device.getCondition()){
                        case 0: image.setImageDrawable(getDrawable(R.drawable.boy0));
                            break;
                        case 1: image.setImageDrawable(getDrawable(R.drawable.boy1));
                            break;
                        case 2: image.setImageDrawable(getDrawable(R.drawable.boy2));
                            break;
                    }

                }else {
                    switch (device.getCondition()){
                        case 0: image.setImageDrawable(getDrawable(R.drawable.girl0));
                            break;
                        case 1: image.setImageDrawable(getDrawable(R.drawable.girl1));
                            break;
                        case 2: image.setImageDrawable(getDrawable(R.drawable.girl2));
                            break;
                    }
                }

                TextView textId = (TextView) holder.itemView.findViewById(R.id.item_id);
                textId.setText(String.format(Locale.US, "%s: %s", getString(R.string.device_id),
                        device.getDeviceId()));

                TextView textRate = (TextView) holder.itemView.findViewById(R.id.item_rate);
                textRate.setText(String.format(Locale.US, "%d", device.getRate()));
                holder.itemView.setOnClickListener(new OnDeviceClick(device));
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class OnDeviceClick implements View.OnClickListener{
            ItemDevice itemDevice;

            OnDeviceClick(ItemDevice itemDevice) {
                this.itemDevice = itemDevice;
            }

            @Override
            public void onClick(View view) {
                //We are passing Bundle to activity, these lines will animate when we laucnh activity
                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(HomeActivity.this,
                        Pair.create(view,getString(R.string.card_transition))
                ).toBundle();

                Intent intent = new Intent(HomeActivity.this, DetailActivity.class);
                intent.putExtra(getString(R.string.key_name), itemDevice.getName());
                intent.putExtra(getString(R.string.key_id), itemDevice.getDeviceId());
                intent.putExtra(getString(R.string.key_gender), itemDevice.isMale());
                intent.putExtra(getString(R.string.key_rate), itemDevice.getRate());
                intent.putExtra(getString(R.string.key_condition), itemDevice.getCondition());

                Log.i("HomeAct", "onClick: "+itemDevice.getRate());

                startActivity(intent, bundle);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_notif:
                startActivity(new Intent(HomeActivity.this, NotificationActivity.class));
                break;
            case R.id.action_about:
                startActivity(new Intent(HomeActivity.this, AboutActivity.class));
                break;
            case R.id.action_logout:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
