/**
 * Created by maakbar on 8/3/2016.
 */
var PanTomkins = require('./P_Tom_Algo.js');
var algo = new PanTomkins();

function Detection() {
    this.addData = function (signal) {
        console.log(signal);

        var volt = convertAdcToVolt(signal);

        //boolean QRS_detected = detectQRS(volt);
        var isQrs =  algo.isQrs(signal);

        var bpm = 0;

        if (isQrs) {
            bpm = calculateBPM();
        }

        return [signal, volt, isQrs, bpm];
    };

    function convertAdcToVolt(adc_val) {
        var volt = 5 * (adc_val / 1023);
        return volt;
    }

    /*FOUND TIME BELUM*/
    function calculateBPM(foundTimeMicros) {

        bpm = (60.0 / (((float)(foundTimeMicros - old_foundTimeMicros)) / 1000000.0));
        old_foundTimeMicros = foundTimeMicros;
        return bpm;
    }

    function isPAC(){  /* Premature Atrium / Ventricular Contraction */
        return false;
    }

    function isSVT(){  /* Super Ventricular Tachycardia (BPM >= 150) */
        return false;
    }

    function isBd(){  /* Bradycardia (BPM <= 60) */
        return false;
    }

}

module.exports = Detection;
