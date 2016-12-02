#include <Arduino.h>
#include "Connector.h"
#include "Reader.h"

// #include "Ticker.h"

// REPEATER
// Ticker repeater;

Connector connector;
Reader reader;

void setup(/* arguments */) {
  /* code */
  Serial.begin(115200);
  delay(5/*second*/*1000/*ms*/);  // wait 5s so the Serial Monitor can connect
  connector.setupConnection();
}

void loop(/* arguments */) {
  // read data
  int readResult = reader.read();
  char str_temp[6];
	dtostrf(readResult, 4, 2, str_temp);
  connector.publish(str_temp);

  // connector.loop();
  delay(3); // loop every 3 ms

  // Serial.println("alif cuy");
  // delay(1000);
}
