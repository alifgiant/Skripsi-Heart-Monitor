#include <Ticker.h>
#include "reading.h"
#define ECG_BUFF_SIZE 150

const int sampleDelay = 3; // ms

Reading reader (sampleDelay);

// REPEATER
Ticker repeater;

// BUFFER VARIABEL
ECG buffer_ecg[ECG_BUFF_SIZE];
int ECG_BUFF_IDX = 0;
int SENDER_BUFF_IDX = 0;
int data_send_counter = 0;

void setup() {
  // boudrate
  Serial.begin(115200);
  // put your setup code here, to run once:

  Serial.println("Device boot");

  setup_connection();

  // Put reading process to ticker. Ticker Callback will be called (readPulse)
  Serial.println("Reading begin");
  repeater.attach_ms(reader.ms, reader.readPulse);
  //repeater.attach(1, reader.readPulse);
}

void loop() {
  // try to send data
  connection_loop();
}
