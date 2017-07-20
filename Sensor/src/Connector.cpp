#include <ESP8266WiFi.h>  //https://github.com/esp8266/Arduino
#include <WiFiManager.h>  //https://github.com/tzapu/WiFiManager
#include <PubSubClient.h>
#include <string>
#include "Connector.h"

#define EQUAL 0

Connector::Connector(){
  TOPIC += SENSOR_ID;
  TOPIC += "/";
  TOPIC += OUT_CHANNEL;
}

void Connector::connectWifi(){
  WiFiManager wifiManager;
  wifiManager.autoConnect("AutoConnectAP");
  Serial.println("Connected to Saved/Selected Wifi :)");
}

WiFiClient espClient;
PubSubClient client (espClient);

void Connector::connectMqtt(){
  client.setServer(Connector::MQTT_BROKER_ADDRESS, Connector::MQTT_PORT);

  Serial.print("Attempting MQTT connection...");
  while (!client.connected()) {
    client.connect(SENSOR_ID);
    Serial.print(".");
    delay(5 /*second*/ *1000 /*millisecond*/);
  }
  Serial.println("Connected");
}
//
std::string Connector::buildMessage(int sample){
  char message[10]; // message buffer
  sprintf (message, "%d:%d", index, sample);
  index += 1; // increase index
  if (index > INDEX_LIMIT) index = 0;  // reset index
  return message;
}

void Connector::publish(int sample){
  std::string message = buildMessage(sample); // configure message
  // Serial.println(message.c_str()); // test builder

  client.publish(TOPIC.c_str(), message.c_str()); // publish
}

void Connector::setupConnection(){
  connectWifi();
  connectMqtt();
}
void Connector::loop(){
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("");
    Serial.println("Disconnected: Re-Try on 3s");
    Serial.println("");
    setupConnection();
    delay(3/*second*/*1000/*ms*/);  // reconnecting after waiting 3 s
  }else{
    // Serial.println("Still Connected");
  }
}
