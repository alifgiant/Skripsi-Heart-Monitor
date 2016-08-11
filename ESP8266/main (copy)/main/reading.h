typedef void (*timer_callback)(void);

struct ECG {
	int raw;
	bool isQrs;
	float bpm;
};

//  PIN MODE
const int pulsePin = 0;  // Pulse Sensor purple wire connected to analog pin 0
const int blinkPin = 13; // pin to blink led at each beat

class Reading
{
public:
	int ms; //

	Reading(int ms);
	static void readPulse();
	static bool detectQRS(float new_ecg);
	static unsigned long calculateBPM ();
};
