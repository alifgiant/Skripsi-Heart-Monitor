#include <Ticker.h>
#include "Reading.h"
#define ECG_BUFF_SIZE 100

const int sampleDelay = 3; // ms

Reading reader (sampleDelay);

// REPEATER
Ticker repeater;

// BUFFER VARIABEL
ECG buffer_ecg[ECG_BUFF_SIZE];
int ECG_BUFF_IDX = 0;
int SENDER_BUFF_IDX = 0;

void setup() {
	// boudrate
	Serial.begin(115200);
	// put your setup code here, to run once:

	Serial.println("Device boot");

	// Put reading process to ticker. Ticker Callback will be called (readPulse)
	Serial.println("Reading begin");
	repeater.attach_ms(reader.ms, reader.readPulse);

	//setup_connection();
}

void loop() {
	// try to send data
	//connection_loop();
}
