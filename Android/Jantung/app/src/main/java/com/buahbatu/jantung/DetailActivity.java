package com.buahbatu.jantung;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.buahbatu.jantung.model.Item;
import com.buahbatu.jantung.model.ItemDevice;
import com.buahbatu.jantung.model.Notification;
import com.robinhood.spark.SparkAdapter;
import com.robinhood.spark.SparkView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
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

    @OnClick(R.id.button_sms) void onSmsClick(){
        if (phoneNumber != null) AppSetting.makeASms(DetailActivity.this, phoneNumber);
        else Toast.makeText(this, getString(R.string.no_phone_num), Toast.LENGTH_SHORT).show();
    }
    @OnClick(R.id.button_call) void onCallClick(){
        if (phoneNumber != null) AppSetting.makeACall(DetailActivity.this, phoneNumber);
        else Toast.makeText(this, getString(R.string.no_phone_num), Toast.LENGTH_SHORT).show();
    }

    private MqttAndroidClient mqttClient;

    private List<String> subscribedTopic = new ArrayList<>();
    private List<Integer> ecgData = new ArrayList<>();
    private MyAdapter ecgAdapter = new MyAdapter();

    private String phoneNumber = null;
    private String username;
    private String deviceId;

    void setupMqtt(){
        // mqtt client
        mqttClient = ((MyApp) getApplication()).getClient();
        if (mqttClient!=null) {
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
                            if (deviceId.equals(splitedTopic[0]))
                                itemRate.setText(String.format(Locale.US, "%s", new String(message.getPayload())));
                            break;
                        case "visual":
                            // show to graph
                            ecgData.add(Integer.parseInt(new String(message.getPayload())));
                            if (ecgData.size()>60){
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
        }else {
            Log.e("SetupMqtt", "MQTT CLIENT IS NULL");
        }
    }

    void setupDetail(){
//        Log.i("DETAIL", "setupDetail: begin");
//        AppSetting.AccountInfo accountInfo = AppSetting.getSavedAccount(DetailActivity.this);
        AppSetting.showProgressDialog(DetailActivity.this, "Retrieving data");

        AndroidNetworking.get(String.format(Locale.US, getString(R.string.http_url), getString(R.string.server_ip_address))
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

    @Override
    protected void onResume() {
        super.onResume();
        setupMqtt();
        System.out.println("detail resume subs "+subscribedTopic.size());
        for (String topic:subscribedTopic){
            try {
                mqttClient.subscribe(topic, 0);
            }catch (MqttException ex){
                ex.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("detail pause subs "+deviceId);
        for (String topic:subscribedTopic){
            try{
                mqttClient.unsubscribe(topic);
            }catch (MqttException ex){
                // do nothing
                // un-subscribe failed
            }

        }
    }

    class MyAdapter extends SparkAdapter {
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
        deviceId = getIntent().getStringExtra(getString(R.string.key_id));
        boolean isMale = getIntent().getBooleanExtra(getString(R.string.key_gender), false);
        int rate = getIntent().getIntExtra(getString(R.string.key_rate), 1000);
        int condition = getIntent().getIntExtra(getString(R.string.key_condition), 0);

        /*MQTT RELATED*/
        subscribedTopic.add(deviceId+"/bpm");
        subscribedTopic.add(deviceId+"/visual");
        subscribedTopic.add(deviceId+"/alert");
        sparkView.setAdapter(ecgAdapter);

        /*DETAIL INFORMATION*/
        setupDetail();
        setupCondition(isMale, condition);

        Log.i("DetailAct", "onCreate: "+rate);

        itemName.setText(username);
        itemId.setText(String.format(Locale.US, "%s: %s", getString(R.string.device_id),
                deviceId));

        itemRate.setText(String.format(Locale.US, "%d", rate));
    }
}
