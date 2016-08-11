#include <ESP8266WiFi.h>
#include <PubSubClient.h>

WiFiClient espClient;
PubSubClient client(espClient);
const char * sensor_id = "02WXO01";
const char * topic_head = "stream/";
const char * ssid = "G40-Onboard";
const char * password = "qwerty123";
// const char* mqtt_server = "192.168.137.1"; //my pc IP
const char * mqtt_server = "10.42.0.1"; //dede pc IP
const int mqtt_port = 1883;
void setup_connection() {
  setup_wifi();
  connect_mqtt();
}
void setup_wifi() {
  delay(10);
  Serial.print("Connecting to ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}
void connect_mqtt() {
  Serial.println("MQTT connection");
  client.setServer(mqtt_server, 1883);
}
void reconnect() {
  // Loop until we're reconnected
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    // Attempt to connect
    if (client.connect(sensor_id)) {
      Serial.println("connected");
      // Once connected, publish an announcement...
      client.publish("outTopic", "reconnecting");
      //Serial.println("setup read");
      //setup_ticker();
      //Serial.println("after setup");
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      // Wait 5 seconds before retrying
      delay(5000);
    }
  }
}
void send_message() {
  //Serial.println(SENDER_BUFF_IDX );
  //Serial.println("ada" + String(SENDER_BUFF_IDX % ECG_BUFF_SIZE));
  if (SENDER_BUFF_IDX < ECG_BUFF_IDX || (SENDER_BUFF_IDX == 99 and ECG_BUFF_IDX == 0)) {
    Serial.println("ada data");
    if (client.connected()) {
      Serial.println("kirim");
      //client.publish(sensor_id, "tes1");
      char message_buff[30];      
      
      String pubString = String(buffer_ecg[SENDER_BUFF_IDX].raw) + "," + String(buffer_ecg[SENDER_BUFF_IDX].isQrs) + "," + String(buffer_ecg[SENDER_BUFF_IDX++].bpm) + "," + String(data_send_counter++);
      //String pubString = "asas";
      
      SENDER_BUFF_IDX %= ECG_BUFF_SIZE;
      data_send_counter %= 20000;
      pubString.toCharArray(message_buff, 30);
      //Serial.println(pubString);
      //sprintf (message_buff, "%03i", data);
      String topic = String(topic_head) + String(sensor_id);
      char topic_buff[topic.length() + 1];
      // Copy it over
      topic.toCharArray(topic_buff, topic.length());
      client.publish(topic_buff, message_buff);
    } else {
      reconnect();
    }
  }
}
void connection_loop() {
  send_message();
}
