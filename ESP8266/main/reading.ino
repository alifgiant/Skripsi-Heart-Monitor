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

//convertToVoltage()
//Does the calculation to convert the Arduino's analog-to-digital
//converter number to a voltage. This function then returns the
//value to the rest of the program.
float convertToVoltage(float ADC_Val)
{
	float volt = 0;

	//please put your calculation in between
	//the "=" and the ";"
	//NOTE: you will use the variable "ADC_Val" for your
	//calculation. It is case senstive and must be written with
	//the underscore as well. It does not include the quotation
	//marks (of course). It is the value currently outputted
	//by your analog to digital converter

	volt = 5 * (ADC_Val / 1023);

	return volt;
}

void Reading::readPulse() {
	int claimedIndex = ECG_BUFF_IDX++;
	ECG_BUFF_IDX %= ECG_BUFF_SIZE;

	int Signal;     // holds the incoming raw data
	Signal = analogRead(pulsePin);              // read the Pulse Sensor

	float volt = convertToVoltage(Signal);
	//boolean QRS_detected = detectQRS(volt);
	boolean QRS_detected = detectQRS(Signal);

	unsigned long bpm = 0;

	if (QRS_detected)
	{
		bpm = calculateBPM();
	}
	char str_temp[6];
	dtostrf(volt, 4, 2, str_temp);

	//Serial.println(volt);
	//Serial.println(Signal);
	Serial.printf("raw: %s, qrs: %s, bpm: %d\n", str_temp, QRS_detected ? "true" : "false", bpm);
	buffer_ecg[claimedIndex].volt = volt;
	buffer_ecg[claimedIndex].isQrs = QRS_detected;
	buffer_ecg[claimedIndex].bpm = bpm;
}

unsigned long Reading::calculateBPM () {
	foundTimeMicros = micros();
	unsigned long bpm = (60.0 / (((float) (foundTimeMicros - old_foundTimeMicros)) / 1000000.0));
	old_foundTimeMicros = foundTimeMicros;
	return bpm;
}


/* Portion pertaining to Pan-Tompkins QRS detection */


// circular buffer for input ecg signal
// we need to keep a history of M + 1 samples for HP filter
float ecg_buff[M + 1] = {0};
int ecg_buff_WR_idx = 0;
int ecg_buff_RD_idx = 0;

// circular buffer for input ecg signal
// we need to keep a history of N+1 samples for LP filter
float hp_buff[N + 1] = {0};
int hp_buff_WR_idx = 0;
int hp_buff_RD_idx = 0;

// LP filter outputs a single point for every input point
// This goes straight to adaptive filtering for eval
float next_eval_pt = 0;

// running sums for HP and LP filters, values shifted in FILO
float hp_sum = 0;
float lp_sum = 0;

// working variables for adaptive thresholding
float treshold = 0;
boolean triggered = false;
int trig_time = 0;
float win_max = 0;
int win_idx = 0;

// numebr of starting iterations, used determine when moving windows are filled
int number_iter = 0;

bool Reading::detectQRS(float new_ecg_pt) {
	// copy new point into circular buffer, increment index
	ecg_buff[ecg_buff_WR_idx++] = new_ecg_pt;
	ecg_buff_WR_idx %= (M + 1);


	/* High pass filtering */
	if (number_iter < M) {
		// first fill buffer with enough points for HP filter
		hp_sum += ecg_buff[ecg_buff_RD_idx];
		hp_buff[hp_buff_WR_idx] = 0;
	}
	else {
		hp_sum += ecg_buff[ecg_buff_RD_idx];

		tmp = ecg_buff_RD_idx - M;
		if (tmp < 0) tmp += M + 1;

		hp_sum -= ecg_buff[tmp];

		float y1 = 0;
		float y2 = 0;

		tmp = (ecg_buff_RD_idx - ((M + 1) / 2));
		if (tmp < 0) tmp += M + 1;

		y2 = ecg_buff[tmp];

		y1 = HP_CONSTANT * hp_sum;

		hp_buff[hp_buff_WR_idx] = y2 - y1;
	}

	// done reading ECG buffer, increment position
	ecg_buff_RD_idx++;
	ecg_buff_RD_idx %= (M + 1);

	// done writing to HP buffer, increment position
	hp_buff_WR_idx++;
	hp_buff_WR_idx %= (N + 1);


	/* Low pass filtering */

	// shift in new sample from high pass filter
	lp_sum += hp_buff[hp_buff_RD_idx] * hp_buff[hp_buff_RD_idx];

	if (number_iter < N) {
		// first fill buffer with enough points for LP filter
		next_eval_pt = 0;

	}
	else {
		// shift out oldest data point
		tmp = hp_buff_RD_idx - N;
		if (tmp < 0) tmp += (N + 1);

		lp_sum -= hp_buff[tmp] * hp_buff[tmp];

		next_eval_pt = lp_sum;
	}

	// done reading HP buffer, increment position
	hp_buff_RD_idx++;
	hp_buff_RD_idx %= (N + 1);


	/* Adapative thresholding beat detection */
	// set initial threshold
	if (number_iter < winSize) {
		if (next_eval_pt > treshold) {
			treshold = next_eval_pt;
		}

		// only increment number_iter iff it is less than winSize
		// if it is bigger, then the counter serves no further purpose
		number_iter++;
	}

	// check if detection hold off period has passed
	if (triggered == true) {
		trig_time++;

		if (trig_time >= 100) {
			triggered = false;
			trig_time = 0;
		}
	}

	// find if we have a new max
	if (next_eval_pt > win_max) win_max = next_eval_pt;

	// find if we are above adaptive threshold
	if (next_eval_pt > treshold && !triggered) {
		triggered = true;

		return true;
	}
	// else we'll finish the function before returning FALSE,
	// to potentially change threshold

	// adjust adaptive threshold using max of signal found
	// in previous window
	if (win_idx++ >= winSize) {
		// weighting factor for determining the contribution of
		// the current peak value to the threshold adjustment
		float gamma = 0.175;

		// forgetting factor -
		// rate at which we forget old observations
		// choose a random value between 0.01 and 0.1 for this,
		float alpha = 0.01 + ( ((float) random(0, RAND_RES) / (float) (RAND_RES)) * ((0.1 - 0.01)));

		// compute new threshold
		treshold = alpha * gamma * win_max + (1 - alpha) * treshold;

		// reset current window index
		win_idx = 0;
		win_max = -10000000;
	}

	// return false if we didn't detect a new QRS
	return false;
}
