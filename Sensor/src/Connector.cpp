#include <ESP8266WiFi.h>  //https://github.com/esp8266/Arduino
#include <WiFiManager.h>  //https://github.com/tzapu/WiFiManager
#include <PubSubClient.h>
#include "Connector.h"

#define EQUAL 0

using namespace std;

const char * Connector::SENSOR_ID = "H00001";
const char * Connector::MQTT_BROKER = "192.168.0.111";
const int Connector::MQTT_PORT = 1883;
const char * Connector::OUT_TOPIC = "report";
const char * Connector::IN_TOPIC = "setting";

void Connector::connectWifi(){
  WiFiManager wifiManager;
  wifiManager.autoConnect("AutoConnectAP");
  Serial.println("connected...yeey :)");
}

void messageArriveCallback(char* topic, byte* payload, unsigned int length){
  // handle message arrived
  Serial.print("Message arrived [");
  Serial.print(topic);
  Serial.print("] ");
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }
}

WiFiClient espClient;
PubSubClient client (Connector::MQTT_BROKER, Connector::MQTT_PORT, messageArriveCallback, espClient);

void Connector::connectMqtt(){
  client.connect(SENSOR_ID);
  Serial.print("Attempting MQTT connection...");
  while (!client.connected()) {
   Serial.print(".");
   delay(5000);
  }
  Serial.println("Connected");
  subscribeMQTT();
}
void Connector::subscribeMQTT(){
  // ... and resubscribe
  Serial.println("Subs setting");
  client.subscribe(IN_TOPIC);
}
void Connector::publish(char* message){
  Serial.println("published");
  string a = "";
  a += OUT_TOPIC;
  a += "/";
  a += SENSOR_ID;
  client.publish(a.c_str(), message);
}
void Connector::setupConnection(){
  connectWifi();
  connectMqtt();
}
void Connector::loop(){
  // if (!WiFi.status() != WL_CONNECTED) {
  //   Serial.println("3. Re-Try on 10s");
  //   setupConnection();
  //   delay(10/*second*/*1000/*ms*/);  // re-try connection
  // }else{
  client.loop();
  // }
}
