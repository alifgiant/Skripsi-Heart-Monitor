#include "reading.h"

void setup() {
	// put your setup code here, to run once:
	Serial.begin(115200);
	setup_connection();  
}

void loop() {
  // put your main code here, to run repeatedly:
  connection_loop();
}
