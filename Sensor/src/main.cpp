#include <Arduino.h>
#include <queue>
#include "Connector.h"
#include "Reader.h"
#include "user_interface.h"

Connector connector; // My Connector Handler
Reader reader;

std::queue<int> read_buffer; // sample buffer
os_timer_t myTimer; // timer, software interrupt

void timerCallback(void *pArg) {  // interrupt call back
      read_buffer.push(reader.read());  // insert read into buffer, filling
}

void setup_interrupt(void) {
  os_timer_setfn(&myTimer, timerCallback, NULL); // set interrupt callback
  os_timer_arm(&myTimer, 5, true); // setup timer, sample every 5 ms, 200Hz
}

void setup(/* arguments */) {
  Serial.begin(115200); // start boudrate at 115200 Hz
  delay(5 /*second*/ * 1000 /*ms*/);  // wait 5s so the Serial Monitor can connect

  Serial.println("--------------------------");
  Serial.println("TA - Muhammad Alif Akbar");
  Serial.println("--------------------------");

  delay(5 /*second*/ * 1000 /*ms*/);  // wait 5s so the Serial Monitor can connect

  connector.setupConnection(); // setup WiFi connection
  setup_interrupt(); // setup interupt after connected to server.
}

void loop() {
  if (read_buffer.size() > 0){ // check if buffer has filling
    int sample = read_buffer.front(); // get first data

    connector.publish(sample); // send via mqtt
    read_buffer.pop(); // pop first element

    // // ------Buffer Check-----
    // char str_temp[6]; // char of sample
    // dtostrf(read_buffer.size(), 4, 2, str_temp);
    // Serial.println(str_temp);  // check
    // // ------Buffer Check-----

    connector.loop();
  }else{
    yield();
  }
}
