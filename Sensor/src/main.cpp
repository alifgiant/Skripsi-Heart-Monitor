#include <Arduino.h>
#include "Connector.h"
// #include "Ticker.h"

// REPEATER
// Ticker repeater;

Connector connector;

void setup(/* arguments */) {
  /* code */
  Serial.begin(115200);
  delay(5/*second*/*1000/*ms*/);  // wait 5s so the Serial Monitor can connect
  connector.setupConnection();
}

void loop(/* arguments */) {
  connector.loop();
  // Serial.println(" do something ");
  delay(1000);
  // int signal = analogRead(0);
  // publish(signal);
}
