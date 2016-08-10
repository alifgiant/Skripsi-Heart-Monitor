/**
 * Created by maaakbar on 8/10/16.
 */
const M = 5;
const N = 30;
const winSize = 250;
const HP_CONSTANT = 1 / M;

// resolution of RNG (Random forget rate)
const RAND_RES = 100000000;

/* Portion pertaining to Pan-Tompkins QRS detection */
function PanTomkins() {
    // circular buffer for input ecg signal
    // we need to keep a history of M + 1 samples for HP filter
    var ecg_buff = Array.apply(null, new Array(M + 1)).map(Number.prototype.valueOf, 0);
    var ecg_buff_WR_idx = 0;
    var ecg_buff_RD_idx = 0;

    // circular buffer for input ecg signal
    // we need to keep a history of N+1 samples for LP filter
    var hp_buff = Array.apply(null, new Array(N + 1)).map(Number.prototype.valueOf, 0);
    var hp_buff_WR_idx = 0;
    var hp_buff_RD_idx = 0;

    // LP filter outputs a single point for every input point
    // This goes straight to adaptive filtering for eval
    var next_eval_pt = 0;

    // running sums for HP and LP filters, values shifted in FILO
    var hp_sum = 0;
    var lp_sum = 0;

    // working variables for adaptive thresholding
    var threshold = 0;
    var triggered = false;
    var trig_time = 0;
    var win_max = 0;
    var win_idx = 0;

    // number of starting iterations, used determine when moving windows are filled
    var number_iter = 0;

    function getRandom(min, max) {
        return Math.random() * (max - min) + min;
    }

    this.isQrs = function (new_ecg_pt) {
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

            var tmp = ecg_buff_RD_idx - M;
            if (tmp < 0) tmp += M + 1;

            hp_sum -= ecg_buff[tmp];

            tmp = (ecg_buff_RD_idx - ((M + 1) / 2));
            if (tmp < 0) tmp += M + 1;

            var y2 = ecg_buff[tmp];

            var y1 = HP_CONSTANT * hp_sum;

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


        /* Adaptive threshold beat detection */
        // set initial threshold
        if (number_iter < winSize) {
            if (next_eval_pt > threshold) {
                threshold = next_eval_pt;
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
        if (next_eval_pt > threshold && !triggered) {
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
            var gamma = 0.175;

            // forgetting factor -
            // rate at which we forget old observations
            // choose a random value between 0.01 and 0.1 for this,
            var alpha = 0.01 + ( (getRandom(0, RAND_RES) / (RAND_RES)) * ((0.1 - 0.01)));

            // compute new threshold
            threshold = alpha * gamma * win_max + (1 - alpha) * threshold;

            // reset current window index
            win_idx = 0;
            win_max = -10000000;
        }

        // return false if we didn't detect a new QRS
        return false;
    }
}

module.exports = PanTomkins;

