/**
 * PanTomKins Algorithm
 */
'use strict';
// load math.js
let math = require('mathjs');
let coefficient = require('./coef');

class Filter {
    constructor(coef) {
        this._coef = coef;
        this._samples = new Array(this._coef.length).fill(0);
    }
    execute(data) {
        // .splice (index, delete, item)
        this._samples.splice(0, 0, data);  // put on index 0, do not delete anything, put data
        if (this._samples.length > this._coef.length){
            this._samples.pop () // remove last data, if more than needed in coef
        }

        let result = 0;
        for (let i=0; i < this._coef.length; i++){
            const coef = this._coef[i];
            const sample = this._samples[i];

            result += sample * coef;
        }

        return result
    }
    get_delay() {
        return (this._coef.length - 1) / 2
    }
}

const WINDOW_DURATION = 8;
const VAL_BY_MEAN = 1;
const IDX_BY_R = 0.5;

class Detector{
    constructor(freq){
        this._freq = freq;
        // filter
        this.low_high_filter = new Filter(coefficient.filter);
        this.derr_filter = new Filter(coefficient.derr);
        this.mwi_filter = new Filter(coefficient.mwi);

        // data holder
        this.sample = [];
        this.peaks_position = [];
        this.rr_distance = [];

        // flags
        this.r_distance = 0
    }

    execute_buffer(){
        const mean = math.mean(this.sample);
        const threshold = VAL_BY_MEAN * mean;

        // peak flags
        let is_peak_area = false;
        let temp_peak_val = -1;

        // peak location holder
        let peaks = [];

        // find peaks in windows
        for (let idx=0; idx < this.sample.length; idx++){
            const val = this.sample[idx];
            if (val > threshold){
                if (!is_peak_area){  // peak area just begin
                    is_peak_area = true;
                    temp_peak_val = val;
                    peaks.push(idx);
                }else if(val > temp_peak_val){  // is in peak area, and current val higher than last
                    // set peak to current val
                    temp_peak_val = val;
                    // update last peak position
                    peaks[peaks.length - 1] = idx;
                }
            }else {
                is_peak_area = false;
            }
        }

        if (peaks.length > 0){
            // calc r-dis average,
            // distance from last window r + idx of last peak
            const r_avg = (this.r_distance + peaks[peaks.length - 1]) / peaks.length;
            const r_threshold = r_avg * IDX_BY_R;

            // add last peak position of last window
            peaks.splice(0, 0, -this.r_distance);  // put on index 0, do not delete anything, put data

            // remove false peak
            let last_picked = 0;
            this.peaks_position = []; // empty peaks of last window
            this.rr_distance = []; // empty peaks of last window

            for (let idx=1; idx < peaks.length; idx++){
                const current = peaks[idx];
                const last = peaks[last_picked];

                if ((current - last) >= r_threshold){
                    this.peaks_position.push(current);
                    this.rr_distance.push(current - last);
                    last_picked = idx;  // set to current
                }
            }
            // safe this window last r distance to end of window
            // idx+1, because idx start 0
            this.r_distance = this.sample.length - (this.peaks_position[this.peaks_position.length - 1] + 1);
        }

        // clear buffer
        this.sample= [];
        return [this.peaks_position, this.rr_distance];
    }

    execute (data, callback){
        // filtering
        const filtered = this.low_high_filter.execute(data);
        const derr = this.derr_filter.execute(filtered);
        const squared = math.pow(derr, 2);
        const mwi = this.mwi_filter.execute(squared);
        
        callback('filter', derr);  // tell filtered result to other devices        

        // load data to window
        this.sample.push(mwi);

        // buffer period is (n * freq) of incoming signal.
        // find peaks every buffer period (seconds).
        // if len(self.sample) == (self.buffer_duration * self.freq):
        if (this.sample.length % (WINDOW_DURATION * this._freq) === 0){
            callback('beat', this.execute_buffer());
        }
    }
}

module.exports = Detector;

if (typeof require !== 'undefined' && require.main === module) {
    let asd = new Detector(200);
    let i = 0;
    while (true){
        asd.execute(1, (type, filtered) => {
            console.log(i, type, filtered);
            i+=1;
        });
    }
}