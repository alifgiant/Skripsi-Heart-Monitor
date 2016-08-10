/**
 * Created by maakbar on 8/3/2016.
 */
const HEART_MAX_CONST = 220;
/* http://www.livescience.com/42081-normal-heart-rate.html */
const R_BUFFER_SIZE = 4;
const SAMPLING_SPEED = 3; // milli second
/* Jun Liu, Yaqi Zhou; Design of a novel portable ECG monitor for heart health; 2013 */

var PanTomkins = require('./P_Tom_Algo.js');
var algo = new PanTomkins();


function Detection() {
    var R_buffer = Array.apply(null, new Array(R_BUFFER_SIZE)).map(Number.prototype.valueOf, 0);
    var R_iter = 0;
    var R_BUFFER_IDX = 0;

    var R_distance = 0;

    this.addData = function (signal) {
        console.log(signal);

        var volt = convertAdcToVolt(signal);

        //boolean QRS_detected = detectQRS(volt);
        var isQrs = algo.isQrs(signal);

        var bpm = 0;
        var prediction = [false, false, false, false, false, true];

        if (isQrs) {
            R_buffer[R_BUFFER_IDX++] = R_distance;
            R_BUFFER_IDX %= R_BUFFER_SIZE;

            bpm = calculateBPM(R_distance);

            if (R_iter < R_BUFFER_SIZE) R_iter += 1;
            else {
                prediction[0] = isPAC(R_distance);
                prediction[1] = isAtrialtachycardia();
                prediction[2] = isBundleBranchBlock(R_distance);
            }

            prediction[3] = isSVT(bpm);            
            prediction[4] = isBd(bpm);
            prediction[5] = isTargetHeartRate(bpm, 22);

            R_distance = 0;
        }

        return [signal, volt, isQrs, bpm, prediction];
    };

    function convertAdcToVolt(adc_val) {
        var volt = 5 * (adc_val / 1023);
        return volt;
    }

    function calculateBPM(current_R) {
        var bpm = 60.0 / (current_R * SAMPLING_SPEED) / 1000.0;
        return bpm;
    }

    /* Premature Atrium / Ventricular Contraction */

    function get4Rmean() {
        var total = 0;
        for (var i = 0; i < R_buffer.length; i++) {
            total += R_buffer[i];
        }
        var avg = total / R_buffer.length;
        return avg;
    }

    function isPAC(current_R) { // or isPVC
        if (R_iter < R_BUFFER_SIZE) {
            pacCounter = 0;
            return false;
        } else {
            var ispac = current_R <= 0.8 * get4Rmean();
            if (ispac) pacCounter += 1;
            else pacCounter = 0;
            return ispac;
        }
    }

    var pacCounter = 0;
    function isAtrialtachycardia() {
        return pacCounter >=  3;
    }

    function isBundleBranchBlock(current_R) {
        if (R_iter < R_BUFFER_SIZE) {
            return false;
        } else {
            return 0.8 * get4Rmean() < current_R && 1.2 * get4Rmean();
        }
    }

    function isSVT(bpm) {  /* Super Ventricular Tachycardia (BPM >= 150) */
        return bpm >= 150;
    }

    function isBd(bpm) {  /* Bradycardia (BPM <= 60) */
        return bpm <= 60;
    }

    function isTargetHeartRate(bpm, age) {
        if (age < 40)
            var maximumHeartRate = HEART_MAX_CONST - age;
        else
            maximumHeartRate = 208 - (0.75 * age);
        var lowerZone = 0.5 * maximumHeartRate;
        var higerZone = 0.85 * maximumHeartRate;
        return lowerZone <= bpm && bpm <= higerZone;
    }
}

module.exports = Detection;
