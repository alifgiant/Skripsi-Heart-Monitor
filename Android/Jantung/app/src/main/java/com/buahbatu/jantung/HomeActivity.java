package com.buahbatu.jantung;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
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

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.buahbatu.jantung.misc.ViewHolder;
import com.buahbatu.jantung.model.ItemDevice;
import com.buahbatu.jantung.model.Item;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private AppSetting.AccountInfo accountInfo;
    private ViewAdapter viewAdapter;
    private MqttAndroidClient mqttClient;

    private List<String> subscribedTopic = new ArrayList<>();

    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @OnClick(R.id.fab) void onFabClick(){
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle(getString(R.string.add_device));

        // Set up the input
        final TextInputEditText friendUsername = new TextInputEditText(this);
        friendUsername.setInputType(InputType.TYPE_CLASS_TEXT);
        friendUsername.setHint(R.string.username);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(friendUsername);

        builder.setView(linearLayout);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // ganti nanti dengan cuma add ke notif
                AppSetting.showProgressDialog(HomeActivity.this, "Adding Friend");
                AndroidNetworking.post(AppSetting.getHttpAddress(HomeActivity.this)
                        +"/{user}/{username}/data/add")
                        .addPathParameter("user", "patient")
                        .addPathParameter("username", accountInfo.username)
                        .addBodyParameter("username", friendUsername.getText().toString())
                        .setPriority(Priority.MEDIUM)
                        .build().getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        AppSetting.dismissProgressDialog();
                        try {
                            Toast.makeText(HomeActivity.this, response.getString("info"), Toast.LENGTH_SHORT).show();
                            items.add(new ItemDevice(response.getString("name"), response.getString("full_name"), DEVICE, response.getString("device_id"), response.getBoolean("is_male")));
                            viewAdapter.notifyDataSetChanged();
                        }catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        AppSetting.dismissProgressDialog();
                        try {
                            JSONObject response = new JSONObject(anError.getErrorBody());
                            Toast.makeText(HomeActivity.this, response.getString("info"), Toast.LENGTH_SHORT).show();
                        }catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }
                });
//                items.add(new ItemDevice("Newly Added", DEVICE, friendUsername.getText().toString(), true));
//                recyclerView.scrollToPosition(items.size()-1);
//                Toast.makeText(HomeActivity.this, "A family/friend added", Toast.LENGTH_SHORT).show();
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

    void getDevicesData(){
        // remove all element
        items.clear();

        AppSetting.showProgressDialog(HomeActivity.this, "Retrieving data");
        items.add(new Item("My Device", HEADER));

        AndroidNetworking.get(AppSetting.getHttpAddress(HomeActivity.this)
                +"/{user}/{username}/data")
                .addPathParameter("user", "patient")
                .addPathParameter("username", accountInfo.username)
                .setPriority(Priority.MEDIUM).build()
                .getAsJSONObject(new JSONObjectRequestListener(){
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("HOME", "onResponse: "+response.toString());
                        AppSetting.dismissProgressDialog();
                        try {
                            String deviceId = response.getString("device_id");
                            String full_name = response.getString("full_name");
                            boolean is_male = response.getBoolean("is_male");

                            subscribedTopic.add(deviceId+"/bpm");

                            // add my device
                            items.add(new ItemDevice(accountInfo.username, full_name, DEVICE, deviceId, is_male));

                            // add friend list header
                            items.add(new Item("Friend Device", HEADER));

                            // add friend devices
                            JSONArray friendArray = response.getJSONArray("friends");
                            for (int i = 0; i < friendArray.length(); i++) {
                                JSONObject object = friendArray.getJSONObject(i);
                                items.add(new ItemDevice(object.getString("name"), object.getString("full_name"),
                                        DEVICE, object.getString("device_id"), object.getBoolean("is_male")));
                                subscribedTopic.add(object.getString("device_id")+"/bpm");
                            }

                            // notify all view
                            viewAdapter.notifyDataSetChanged();
                        }catch (JSONException ex){
                            ex.printStackTrace();
                        }
                        onResume();
                    }
                    @Override
                    public void onError(ANError anError) {
                        AppSetting.dismissProgressDialog();
                        Log.i("HOME", "onError: "+anError.getErrorBody());
                        onResume();
                    }
                });
    }

    void setupMqttCallBack(){
        // mqtt client
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("Connection was lost!");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
//                System.out.println("Message Arrived!: " + topic + ": " + new String(message.getPayload()));
                String[] splitedTopic = topic.split("/");
                switch (splitedTopic[1]) {
                    case "bpm":
                        for (Item item : items) {
                            if (item.getItemType() == DEVICE) {
                                ItemDevice device = (ItemDevice) item;
                                if (device.getDeviceId().equals(splitedTopic[0])) {
                                    device.setRate(Float.parseFloat(new String(message.getPayload())));
                                    viewAdapter.notifyDataSetChanged();
//                                        break;
                                }
                            }
                        }
                        break;
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                System.out.println("Delivery Complete!");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupMqttCallBack();
        System.out.println("resume subs "+subscribedTopic.size());
        for (String topic:subscribedTopic){
            try {
                mqttClient.subscribe(topic, 0);//
            }catch (MqttException ex){
                ex.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
    Log.i("Detail", "onDestroy: ");
    for (String topic:subscribedTopic){
        Log.i("Detail", "onDestroy: "+topic);
        try{
            mqttClient.unsubscribe(topic);
        }catch (MqttException ex){
            // do nothing
            // un-subscribe failed
        }
    }

    mqttClient.unregisterResources();
    mqttClient.close();
    super.onDestroy();
}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        accountInfo = AppSetting.getSavedAccount(HomeActivity.this);

        // init adapter and data holder
        viewAdapter = new ViewAdapter();
        items = new ArrayList<>();

        if (mqttClient == null) {
            mqttClient = AppSetting.getMqttClient(HomeActivity.this);
            try {
                System.out.println("Setup Mqtt");
                mqttClient.connect();
            }catch (MqttException ex){
                Log.e("MqttSetup", "can't connect");
                ex.printStackTrace();
            }
        }
        getDevicesData();

        recyclerView.setLayoutManager(new LinearLayoutManager(HomeActivity.this));
        recyclerView.setAdapter(viewAdapter);
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
            final Item item = items.get(position);
            TextView textName = (TextView) holder.itemView.findViewById(R.id.item_name);
            textName.setText(item.getName());

            if (item.getItemType() == DEVICE){
                final ItemDevice device = (ItemDevice)item;
                textName.setText(device.getFull_name());
                ImageView image = (ImageView) holder.itemView.findViewById(R.id.item_image);
                if (device.isMale()){
                    switch (device.getCondition()){
                        case 0: image.setImageDrawable(ContextCompat.getDrawable(HomeActivity.this, R.drawable.boy0));
                            break;
                        case 1: image.setImageDrawable(ContextCompat.getDrawable(HomeActivity.this, R.drawable.boy1));
                            break;
                        case 2: image.setImageDrawable(ContextCompat.getDrawable(HomeActivity.this, R.drawable.boy2));
                            break;
                    }

                }else {
                    switch (device.getCondition()){
                        case 0: image.setImageDrawable(ContextCompat.getDrawable(HomeActivity.this,R.drawable.girl0));
                            break;
                        case 1: image.setImageDrawable(ContextCompat.getDrawable(HomeActivity.this,R.drawable.girl1));
                            break;
                        case 2: image.setImageDrawable(ContextCompat.getDrawable(HomeActivity.this,R.drawable.girl2));
                            break;
                    }
                }

                TextView textId = (TextView) holder.itemView.findViewById(R.id.item_id);
                textId.setText(String.format(Locale.US, "%s: %s", getString(R.string.device_id),
                        device.getDeviceId()));

                TextView textRate = (TextView) holder.itemView.findViewById(R.id.item_rate);
                textRate.setText(String.format(Locale.US, "%.0f", device.getRate()));
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
                intent.putExtra(getString(R.string.key_user_name), accountInfo.username);
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
                AppSetting.setLogin(HomeActivity.this, AppSetting.LOGGED_OUT);

                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
