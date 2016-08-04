#include <Ticker.h>
#include "Reading.h"

const int sampleDelay = 3; // ms

Reading reader (sampleDelay);
Ticker repeater;

void setup() {
	// boudrate
	Serial.begin(115200);
	// put your setup code here, to run once:

	Serial.println("Device boot");

	// Put reading process to ticker. Ticker Callback will be called (readPulse)
	Serial.println("Reading begin");
	repeater.attach_ms(reader.ms, reader.readPulse);

	// setup_connection();
}

void loop() {
	// try to send data
}
