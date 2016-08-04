typedef void (*timer_callback)(void);

struct ECG {
	float volt;
	bool isQrs;
	float bpm;
};

//  PIN MODE
const int pulsePin = 0;  // Pulse Sensor purple wire connected to analog pin 0
const int blinkPin = 13; // pin to blink led at each beat

// BUFFER VARIABEL
ECG buffer_ecg[1000];

int ECG_BUFF_IDX = 0;

class Reading
{
public:
	int ms; //

	Reading(int ms);
	static void readPulse();
	static bool detectQRS(float new_ecg);
};
