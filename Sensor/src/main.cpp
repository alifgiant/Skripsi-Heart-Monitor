#include <Arduino.h>
// #include "Connector.h"
// #include "Ticker.h"

// REPEATER
// Ticker repeater;

// Connector connector;

void setup(/* arguments */) {
  /* code */
  Serial.begin(115200);
  // delay(5000);  // wait 5s so the Serial Monitor can connect
  // connector.setupConnection();
  // setup_wifi();
  // connect_mqtt();
  // reconnect();
}

void loop(/* arguments */) {
  // connector.loop();
  Serial.println(" do something ");
  delay(5000);
  // int signal = analogRead(0);
  // publish(signal);
}
