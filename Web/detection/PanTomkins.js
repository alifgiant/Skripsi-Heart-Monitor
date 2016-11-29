/**
 * PanTomKins Algorithm
 */
const freqr = require("freqr");

function prcess(data_source) {
    /**
     * LOW PASS FILTERING
     */
    b = [1, 0, 0, 0, 0, 0, -2, 0, 0, 0, 0, 0, 1];
    a = [1, -2, 1];
    lowed = lfilter(b, a, data_source);
}

// type: "lowpass", freq: 2400hz, Q: 8
const b = [ 0.027136,  0.054272, 0.027136 ];
const a = [ 1.000000, -1.766316, 0.874860 ];

for (var f = 0; f < 0.2; f += 0.0025) {
    const res = freqr(b, a, f);

    console.log(res);
}