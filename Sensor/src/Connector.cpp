#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include "Connector.h"

#define EQUAL 0

using namespace std;

const char * Connector::DEFAULT_SSID = "MyPulseSensor";
const char * Connector::DEFAULT_PASS = "12345678";

const char * Connector::SENSOR_ID = "H00001";
const int Connector::MQTT_PORT = 1883;
const char * Connector::OUT_TOPIC = "report";
const char * Connector::IN_TOPIC = "setting";
bool Connector::IS_CONNECTED = false;

void Connector::connectWifi(const char * ssid, const char * pass){
  // check if saved network exist
  Serial.println("0. Scanning..");
  int n = WiFi.scanNetworks();
  // int n = 1;
  if (n == 0)
    Serial.println("1. No networks found");
  else if (n > 0) {
    Serial.println("1. Networks found");
    bool ssidFound = false;
    // bool ssidFound = true;
    for (int i = 0; i < n; ++i)
    {
      WiFi.SSID(i);
      // // Print SSID and RSSI for each network found
      Serial.print(": ");
      Serial.print(i + 1);
      Serial.print(": ");
      Serial.print(WiFi.SSID(i));
      Serial.print(" (");
      Serial.print(WiFi.RSSI(i));
      Serial.print(")");
      Serial.println((WiFi.encryptionType(i) == ENC_TYPE_NONE)?" ":"*");
      if (strcmp(WiFi.SSID(i).c_str(), ssid) == EQUAL) { // equal means different is 0
        ssidFound = true;
        break;
      }
    }
    if (ssidFound) {
      Serial.println("2. TARGET SSID IS FOUND");
      // try to connect
      // WiFi.mode(WIFI_STA);
      WiFi.begin(ssid, pass);
      Serial.print("3. Connecting");
      while (WiFi.status() != WL_CONNECTED) {
        delay(1000);
        Serial.print(".");
      }
      Serial.println(".");
      Serial.println("Connected");
      Serial.println("IP address: ");
      Serial.println(WiFi.localIP());
      Connector::IS_CONNECTED = true;
    }else{
      Serial.println("2. TARGET SSID IS NOT FOUND");
    }
  }
}
void Connector::connectMqtt(){
}
void Connector::subscribeMQTT(){
}
void Connector::saveSSIDinfo(const char *ssid, const char *pass){
  // save to eeprom later
}
const char * Connector::loadSSID(){
  // load from eeprom later
  return DEFAULT_SSID;
}
const char * Connector::loadPass(){
  // load from eeprom later
  return DEFAULT_PASS;
}
void Connector::resetConnection(){

}
void Connector::publish(char message[]){

}
void Connector::setupConnection(){
  const char * ssid = loadSSID();
  const char * pass = loadPass();
  connectWifi(ssid, pass);
}
void Connector::loop(){
  if (!IS_CONNECTED) {
    Serial.println("3. Re-Try on 10s");
    setupConnection();
    delay(10/*second*/*1000/*ms*/);  // re-try connection
  }
}
