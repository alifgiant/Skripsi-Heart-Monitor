/**
 * Classifier algorithm based on threshold:
 * An Arrhythmia Classification System Based on the RR-Interval Signal
 * University Ioannina, Greece
 * DOI: 10.1016/j.artmed.2004.03.007 · Source: DBLP
 *
 * all distance or width is on s (Seconds)
 */

var BeatCategory = {
    normal: {code: 1, name: "Normal", annotation: ['N', 'P', 'f', 'p', 'L', 'R',' Q']},
    pvc: {code: 2, name: "PVC", annotation: ['V']},
    vf: {code: 3, name: "Ventricular Flutter/Fibrillation", annotation: ['[', '!', ']']},
    HeartBlock: {code: 4, name: "Heart Block", annotation: ['(BII']}
};

var RhythmCategory = {
    n : {name: 'Normal'},
    b : {name: 'Ventricular Bigeminy'},
    c : {name: 'Ventricular Couplets'},
    t : {name: 'Ventricular Trigeminy'},
    vt : {name: 'Ventricular Tachycardia'},
    vfl : {name: 'Ventricular Flutter/fibrillation'},
    BII : {name: 'heart block'}
};

/**
 * RR-i window with size 3
 * @param {Array} segment
 * @param {int} i
 * @param {int} n
 * @returns {*} an element of segment
 */
function getRR(segment, i, n) {
    if (n === 1)
        return segment[i];  // 0
    else if (n === 2)
        return segment[i+1]; // 1
    else if (n === 3)
        return segment[i+2];  // 2
    else
        throw new Error("RR window is 1 to 3");
}

/**
 * @return {boolean}
 */
function C1(RR1, RR2, RR3) {
    return RR2 < 0.6 && 1.8*RR2 < RR1;
}

/**
 * @return {boolean}
 */
function C2(RR1, RR2, RR3) {
    return (RR1 < 0.7 && RR2 < 0.7 && RR3 < 0.7) || (RR1+RR2+RR3 < 1.7);
}

/**
 * @return {boolean}
 */
function C3(RR1, RR2, RR3) {
    return 1.15*RR2 < RR1 && 1.15*RR2 < RR3;
}

/**
 * @return {boolean}
 */
function C4(RR1, RR2, RR3) {
    return (Math.abs(RR1-RR2)) < 0.3 && (RR1 < 0.8 || RR2 < 0.8) && (RR3 > 1.2*mean([RR1, RR2]));
}

/**
 * @return {boolean}
 */
function C5(RR1, RR2, RR3) {
    return (Math.abs(RR2-RR3)) < 0.3 && (RR2 < 0.8 || RR3 < 0.8) && (RR1 > 1.2*mean([RR2, RR3]));
}

/**
 * @return {boolean}
 */
function C6(RR1, RR2, RR3) {
    return (2.2 < RR2 && RR2 < 3.0) && (Math.abs(RR1-RR2)<0.2 || Math.abs(RR2-RR3)<0.2);
}

/**
 * a support mean function
 * @param {int[]} numbers
 * @returns {number}
 */
function mean(numbers) {
    // mean of [3, 5, 4, 4, 1, 1, 2, 3] is 2.875
    var total = 0,
        i;
    for (i = 0; i < numbers.length; i += 1) {
        total += numbers[i];
    }
    return total / numbers.length;
}

/**
 * @param {float[]} segment 32 element array
 * @returns {Array} of category
 * category:
 * 1 = Normal
 * 2 = PVC
 * 3 = VF
 * 4 = Heart Block
 */
function analyzeBeatSegment(segment) {
    var i = 0;  // first window
    var segmentLength = segment.length;
    var category = [];

    while (i < segmentLength-2) {
        var RR1 = getRR(segment, i, 1);
        var RR2 = getRR(segment, i, 2);
        var RR3 = getRR(segment, i, 3);
        category[i] = BeatCategory.normal; // set category normal
        if (C1(RR1, RR2, RR3)) {
            var pulseCount = 0;
            do {
                category[i] = BeatCategory.vf;  // set category Ventricular Flutter/Fibrillation
                i += 1;  // move to next windows
                pulseCount += 1;

                // get next window RRs
                RR1 = getRR(segment, i, 1);
                RR2 = getRR(segment, i, 2);
                RR3 = getRR(segment, i, 3);
                // console.log('before', category);
            } while (i < segmentLength - 2 /*ignore last 1 element*/
            && C2(RR1, RR2, RR3));
            if (pulseCount < 4) {
                while (pulseCount > 0) {
                    i -= 1;
                    pulseCount -= 1;
                    category[i] = BeatCategory.normal;  // set category normal
                }
            }
        }
        if (C3(RR1, RR2, RR3) && C4(RR1, RR2, RR3) && C5(RR1, RR2, RR3)) {
            category[i] = BeatCategory.pvc;  // set category PVC
        }
        if (C6(RR1, RR2, RR3)) {
            category[i] = BeatCategory.HeartBlock;  // set category Heart Block
        }
        i += 1;  // move to next windows
    }
    return category;
}

/**
 * @param {int} state
 * @param {BeatCategory} beat
 * @returns {int} next state
 */
function finiteStateAutomation(state, beat) {
    if (state == 1){  // Initial stage of the automaton.
        if (beat == BeatCategory.pvc)
            state = 2;
        else if (beat == BeatCategory.vf)
            state = 7;
        else if (beat == BeatCategory.HeartBlock)
            state = 8;
        else  // normal
            state = 1;
    } else if (state == 2){  // Possible ventricular bigeminy, trigeminy, couplet or tachycardia.
        if (beat == BeatCategory.normal)
            state = 3;
        else if (beat == BeatCategory.pvc)
            state = 5;
        else
            state = finiteStateAutomation(1, beat);
    } else if (state == 3){
        if (beat == BeatCategory.pvc)
            state = 2;
        else if (beat == BeatCategory.normal)
            state = 4;
        else
            state = finiteStateAutomation(1, beat);
    } else if (state == 4) {
        if (beat == BeatCategory.pvc)
            state = 2;
        else
            state = finiteStateAutomation(1, beat);
    } else if (state == 5){
        if (beat == BeatCategory.pvc)
            state = 6;
        else if (beat == BeatCategory.normal)
            state = 3;
        else
            state = finiteStateAutomation(1, beat);
    } else if (state == 6){
        if (beat == BeatCategory.pvc)
            state = 6;
        else if (beat == BeatCategory.vf)
            state = 7;
        else if (beat == BeatCategory.HeartBlock)
            state = 8;
        else if (beat == BeatCategory.normal)
            state = 3;
        else
            state = finiteStateAutomation(1, beat);
    } else if (state == 7){
        if (beat == BeatCategory.pvc)
            state = 2;
        else if (beat == BeatCategory.vf)
            state = 7;
        else if (beat == BeatCategory.HeartBlock)
            state = 8;
        else
            state = finiteStateAutomation(1, beat);
    } else if (state == 8){
        if (beat == BeatCategory.pvc)
            state = 2;
        else if (beat == BeatCategory.vf)
            state = 7;
        else if (beat == BeatCategory.HeartBlock)
            state = 8;
        else
            state = finiteStateAutomation(1, beat);
    } else
        state = 1;
    return state;
}

/**
 * @param {BeatCategory[]} segment
 * @return {RhythmCategory[]} rhythm
 */
function analyzeBeatClassification(segment) {
    var i = 0;
    var episodes = [RhythmCategory.n];

    var nextState = 1;
    var lastState = 1;

    var vf = 0;
    var bigeminy = 0;
    var trigeminy = 0;
    var block = 0;

    while (i<segment.length){
        var beat = segment[i];
        lastState = nextState;
        nextState = finiteStateAutomation(nextState, beat);
        if (nextState == 2 && lastState == 3){
            bigeminy += 1;
            if (bigeminy == 2)
                episodes.push(RhythmCategory.b);
            trigeminy = 0;
            vf = 0;
            block = 0;
        } else if (nextState == 2 && lastState == 4){
            trigeminy += 1;
            if (trigeminy == 2)
                episodes.push(RhythmCategory.t);
            bigeminy = 0;
            vf = 0;
            block = 0;
        } else if (nextState == 5) {
            episodes.push(RhythmCategory.c);
            bigeminy = 0;
            trigeminy = 0;
            vf = 0;
            block = 0;
        } else if (nextState == 6) {
            if (lastState != 6)
                episodes.push(RhythmCategory.vt);
            bigeminy = 0;
            trigeminy = 0;
            vf = 0;
            block = 0;
        }else if (nextState == 7){
            vf += 1;
            if (vf == 3)
                episodes.push(RhythmCategory.vfl);
            bigeminy = 0;
            trigeminy = 0;
            block = 0;
        }else if (nextState == 8){
            block += 1;
            if (block == 2)
                episodes.push(RhythmCategory.BII);
            bigeminy = 0;
            trigeminy = 0;
            vf = 0;
        }
        i += 1;
    }
    return episodes;
}

// // data = [1,0.5,3,4,5,6, 0.5, 0.5, 0.5];
// data = [BeatCategory.pvc, BeatCategory.pvc, BeatCategory.normal, BeatCategory.pvc, BeatCategory.normal, BeatCategory.pvc];
//
// result = analyzeBeatClassification(data);
// // console.log(data);
// console.log(result);

module.exports = {
    beatCategory: BeatCategory,
    episodeCategory: RhythmCategory,
    classifyBeat: analyzeBeatSegment,
    classifyEpisode: analyzeBeatClassification
};
