package com.buahbatu.jantung;

import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.buahbatu.jantung.model.Notification;
import com.robinhood.spark.SparkAdapter;
import com.robinhood.spark.SparkView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DetailActivity extends AppCompatActivity {

    /*QUICK INFO*/
    @BindView(R.id.item_image) ImageView itemImage;
    @BindView(R.id.item_name) TextView itemName;
    @BindView(R.id.item_id) TextView itemId;
    @BindView(R.id.item_rate) TextView itemRate;

    /*GRAPH VIEW*/
    @BindView(R.id.graphView) SparkView sparkView;

    /*DETAIL INFO*/
    @BindView(R.id.friend_full_name) TextView friendFullName;
    @BindView(R.id.friend_address) TextView friendAddress;
    @BindView(R.id.friend_phone) TextView friendPhone;
    @BindView(R.id.friend_gender) TextView friendGender;
    @BindView(R.id.friend_age) TextView friendAge;

    /*ALERT INFO*/
    @BindView(R.id.alert_image) ImageView alertImage;
    @BindView(R.id.alert_title) TextView alertTitle;
    @BindView(R.id.alert_detail) TextView alertDetail;
    @BindView(R.id.button_remove) View button_remove;

    @OnClick(R.id.button_sms) void onSmsClick(){
        if (phoneNumber != null) AppSetting.makeASms(DetailActivity.this, phoneNumber);
        else Toast.makeText(this, getString(R.string.no_phone_num), Toast.LENGTH_SHORT).show();
    }
    @OnClick(R.id.button_call) void onCallClick(){
        if (phoneNumber != null) AppSetting.makeACall(DetailActivity.this, phoneNumber);
        else Toast.makeText(this, getString(R.string.no_phone_num), Toast.LENGTH_SHORT).show();
    }
    @OnClick(R.id.button_remove) void onRemoveClick(){
        AppSetting.showProgressDialog(DetailActivity.this, "Removing friend");
        AndroidNetworking.post(AppSetting.getHttpAddress(DetailActivity.this)
                +"/{user}/{username}/data/remove")
                .addPathParameter("user", "patient")
                .addPathParameter("username", my_username)
                .addBodyParameter("username", username)
                .setPriority(Priority.MEDIUM).build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        AppSetting.dismissProgressDialog();
                        Toast.makeText(DetailActivity.this, getString(R.string.friend_delete_success), Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(ANError anError) {
                        AppSetting.dismissProgressDialog();
                        Log.i("Detail", "onError: "+anError.getErrorBody());
                    }
                });
    }

    private MqttAndroidClient mqttClient;

    private List<String> subscribedTopic = new ArrayList<>();
    private List<Float> ecgData = new ArrayList<>();
    private MyAdapter ecgAdapter = new MyAdapter();

    private String phoneNumber = null;
    private String my_username;
    private String username;
    private String deviceId;

    void setupMqttCallBack(){
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("Connection was lost!");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.println("Message Arrived!: " + topic + ": " + new String(message.getPayload()));
                String[] splitedTopic = topic.split("/");
                switch (splitedTopic[1]) {
                    case "bpm":
                        itemRate.setText(String.format(Locale.US, "%.0f", Float.parseFloat(new String(message.getPayload()))));
                        break;
                    case "visual":
                        // show to graph
                        ecgData.add(Float.parseFloat(new String(message.getPayload())));
                        if (ecgData.size()>100){
                            ecgData.remove(0);
                        }
                        ecgAdapter.notifyDataSetChanged();
                        break;
                    case "alert":
                        String alertString = new String(message.getPayload());
                        /*[TITLE, DETAIL, CONDITION]*/
                        String[] splittedAlert = alertString.split("#");

                        Notification notification = new Notification(
                                String.format(Locale.US, splittedAlert[0], username),
                                splittedAlert[1], Integer.parseInt(splittedAlert[2]));

                        alertTitle.setText(notification.getTitle());
                        alertDetail.setText(notification.getDetail());
                        switch (notification.getCondition()){
                            case Notification.HEALTH:
                                alertImage.setImageResource(R.drawable.ic_error_green);
                                break;
                            case Notification.SICK:
                                alertImage.setImageResource(R.drawable.ic_error_yellow);
                                break;
                            case Notification.DANGER:
                                alertImage.setImageResource(R.drawable.ic_error_red);
                                break;
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

    void setupDetail(){
        AppSetting.showProgressDialog(DetailActivity.this, "Retrieving data");

        AndroidNetworking.get(AppSetting.getHttpAddress(DetailActivity.this)
                +"/{user}/{username}/data/simple")
                .addPathParameter("user", "patient")
                .addPathParameter("username", username)
                .setPriority(Priority.MEDIUM).build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        AppSetting.dismissProgressDialog();
                        try{
                            friendFullName.setText(response.getString("full_name"));
                            friendAddress.setText(response.getString("address"));
                            friendPhone.setText(response.getString("phone"));
                            phoneNumber = response.getString("phone"); /*setup phone*/
                            friendGender.setText(response.getBoolean("is_male")?"Male":"Female");
                            friendAge.setText(response.getString("age"));
                        }catch (JSONException ex){
                            // do nothing
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        AppSetting.dismissProgressDialog();
                        Log.i("Detail", "onError: "+anError.getErrorBody());
                    }
                });
    }

    void setupCondition(boolean isMale, int condition){
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
    }

    private void resumeMqtt() {
        setupMqttCallBack();
        System.out.println("detail resume subs "+subscribedTopic.size());
        for (String topic:subscribedTopic){
            System.out.println("detail resume subs "+topic);
            try {
//                if (mqttClient != null)
                    mqttClient.subscribe(topic, 0);
//                else
//                    System.out.println("MQTT is NULL");
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

    private class MyAdapter extends SparkAdapter {

        @Override
        public RectF getDataBounds() {
            final int count = getCount();

            float minY = -0.1f;
            float maxY = 0.1f;
            float minX = Float.MAX_VALUE;
            float maxX = -Float.MAX_VALUE;
            for (int i = 0; i < count; i++) {
                final float x = getX(i);
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);

                final float y = getY(i);
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }

            // set values on the return object
            return new RectF(minX, minY, maxX, maxY);
        }

        @Override
        public int getCount() {
            return ecgData.size();
        }

        @Override
        public Object getItem(int index) {
            return ecgData.get(index);
        }

        @Override
        public float getY(int index) {
            return ecgData.get(index);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        username = getIntent().getStringExtra(getString(R.string.key_name));
        my_username = getIntent().getStringExtra(getString(R.string.key_user_name));
        deviceId = getIntent().getStringExtra(getString(R.string.key_id));
        boolean isMale = getIntent().getBooleanExtra(getString(R.string.key_gender), false);
        float rate = getIntent().getFloatExtra(getString(R.string.key_rate), 1000);
        int condition = getIntent().getIntExtra(getString(R.string.key_condition), 0);

        if (username.equals(AppSetting.getSavedAccount(DetailActivity.this).username)){
            button_remove.setVisibility(View.GONE);
        }

        /*MQTT RELATED*/
        subscribedTopic.add(deviceId+"/bpm");
        subscribedTopic.add(deviceId+"/visual");
        subscribedTopic.add(deviceId+"/alert");
        sparkView.setAdapter(ecgAdapter);

        if (mqttClient == null) {
            mqttClient = AppSetting.getMqttClient(DetailActivity.this);
            try {
                System.out.println("Setup Mqtt");
                mqttClient.connect(DetailActivity.this, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        System.out.print("connected");
                        resumeMqtt();
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                    }
                });
            }catch (MqttException ex){
                Log.e("MqttSetup", "can't connect");
                ex.printStackTrace();
            }
        }

        /*DETAIL INFORMATION*/
        setupDetail();
        setupCondition(isMale, condition);

        Log.i("DetailAct", "onCreate: "+rate);

        itemName.setText(username);
        itemId.setText(String.format(Locale.US, "%s: %s", getString(R.string.device_id),
                deviceId));

        itemRate.setText(String.format(Locale.US, "%.0f", rate));
        itemRate.setText(String.format(Locale.US, "%.0f", rate));
    }
}
