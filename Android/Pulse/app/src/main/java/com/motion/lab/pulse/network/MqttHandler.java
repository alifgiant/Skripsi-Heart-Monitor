package com.motion.lab.pulse.network;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;

/**
 * Handling MqttConnection Publish and Subscribe
 */
public class MqttHandler {
    private final static String TAG = "MqttHandler";
    public enum QOS {QOS_FIRE, QOS_AT_LEAST_ONCE, QOS_EXACT_ONCE}

    private static MqttHandler mqttHandler;

    private String clientId;
    private MqttAndroidClient client;
    private ArrayList<MqttMessageListener> messageListeners;

    public static MqttHandler GetInstance(Context context) throws MqttException{
        if (mqttHandler == null) {
            throw new MqttException(new Throwable("Null mqtt handler, cant create Client Id not exist," +
                    " Invoke GetInstance(Context, ClientId"));
        }else {
            mqttHandler.client.registerResources(context);
        }
        return mqttHandler;
    }

    public static MqttHandler GetInstance(Context context, String clientId) {
        if (mqttHandler == null) {
            mqttHandler = new MqttHandler(context, clientId);
        }else {
            mqttHandler.client.registerResources(context);
        }
        return mqttHandler;
    }

    public static void RemoveInstance(){
        if (mqttHandler != null) {
            while (mqttHandler.messageListeners.size()>0)
                mqttHandler.messageListeners.remove(0);
            try {
                mqttHandler.client.disconnect();
            }catch (MqttException ex){
                ex.printStackTrace();
                Log.e(TAG, "RemoveInstance: "+ex.getMessage());
            }
            mqttHandler = null;
        }
    }

    private MqttHandler(Context context, String clientId) {
        this.clientId = clientId;
        this.client = new MqttAndroidClient(context, Nethelper.getMqttDomain(context), clientId);
        this.messageListeners = new ArrayList<>();
        MqttCallback mqttCallback = new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                for (MqttMessageListener listener: messageListeners) {
                    listener.onMessageArrive(topic, message);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        };
        client.setCallback(mqttCallback);
    }

    public void connect(IMqttActionListener mqttActionListener){
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(mqttActionListener);
        }catch (MqttException e){
            e.printStackTrace();
        }
    }

    public String getClientId() {
        return clientId;
    }

    public void unBind(){
        client.unregisterResources();
    }

    public void disconnect() {
        try {
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String topic, QOS quality){
        int qos = 0;
        switch (quality){
            case QOS_FIRE: qos = 0; break;
            case QOS_AT_LEAST_ONCE: qos = 1; break;
            case QOS_EXACT_ONCE: qos = 2; break;
        }
        try {
            IMqttToken subscToken = client.subscribe("sensor/"+topic, qos);
            subscToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "mqtt subscribe success");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "mqtt subscribe failed");
                }
            });
        }catch (MqttException e){
            e.printStackTrace();
        }
    }

//    public void publish(String topic, String username, String messageText, boolean isMale, String date){
//        try {
//            MqttMessage message = new MqttMessage();
//            message.setQos(1);
//            JSONObject json = new JSONObject();
//            json.put("username", username);
//            json.put("message", messageText);
//            json.put("is_male", isMale);
//            json.put("date", date);
//            Log.i(TAG, "publish: "+json.toString());
//            message.setPayload(json.toString().getBytes());
//            client.publish(topic, message);
//            Log.i(TAG, "onPublish success");
//        }catch (JSONException | MqttException e){
//            Log.i(TAG, "onPublish error");
//            e.printStackTrace();
//        }
//    }

    // region Message Listener
    public MqttMessageListener getListener(int pos){
        return messageListeners.get(pos);
    }

    public void registerListener(MqttMessageListener listener){
        messageListeners.add(listener);
    }

    public void removeListener(MqttMessageListener listener){
        messageListeners.remove(listener);
    }

    public void removeListener(int pos){
        messageListeners.remove(pos);
    }

    public interface MqttMessageListener{
        void onMessageArrive (String topic, MqttMessage message);
    }
    // endregion
}
