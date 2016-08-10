#define M 			5
#define N 			30
#define winSize			250
#define HP_CONSTANT    ((float) 1 / (float) M)

// timing variables
unsigned long previousMicros  = 0;        // will store last time LED was updated
unsigned long foundTimeMicros = 0;        // time at which last QRS was found
unsigned long old_foundTimeMicros = 0;        // time at which QRS before last was found
unsigned long currentMicros   = 0;        // current time

// resolution of RNG (Random forget rate)
#define RAND_RES 100000000
int tmp = 0;

Reading::Reading(int ms) {
	pinMode(blinkPin, OUTPUT);         // pin that will blink to your heartbeat!
	this->ms = ms;
}


void Reading::readPulse() {
	int claimedIndex = ECG_BUFF_IDX++;
	ECG_BUFF_IDX %= ECG_BUFF_SIZE;

	int Signal;     // holds the incoming raw data
	Signal = analogRead(pulsePin);              // read the Pulse Sensor

	
	char str_temp[6];
	dtostrf(Signal, 4, 2, str_temp);

	//Serial.println(volt);
	//Serial.println(Signal);
	Serial.printf("raw: %s\n", str_temp);
	buffer_ecg[claimedIndex] = Signal;	
}
