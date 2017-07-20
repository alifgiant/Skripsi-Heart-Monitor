/**
 * Classifier algorithm based on threshold:
 * An Arrhythmia Classification System Based on the RR-Interval Signal
 * University Ioannina, Greece
 * DOI: 10.1016/j.artmed.2004.03.007 · Source: DBLP
 *
 * all distance or width is on s (Seconds)
 */

let BeatCategory = {
    normal: {code: 1, name: "Normal", annotation: ['N', 'P', 'f', 'p', 'L', 'R',' Q']},
    pvc: {code: 2, name: "PVC", annotation: ['V']},
    vf: {code: 3, name: "Ventricular Flutter/Fibrillation", annotation: ['[', '!', ']']},
    HeartBlock: {code: 4, name: "Heart Block", annotation: ['(BII']}
};

let RhythmCategory = {
    n : {name: 'Normal'},
    b : {name: 'Ventricular Bigeminy'},
    c : {name: 'Ventricular Couplets'},
    t : {name: 'Ventricular Trigeminy'},
    vt : {name: 'Ventricular Tachycardia'},
    vfl : {name: 'Ventricular Flutter/fibrillation'},
    BII : {name: 'heart block'}
};

class BeatClassifier{
    constructor(sampling_freq){
        // experiment variable
        this.freq = sampling_freq;  // total sample in a second

        // holder
        this.rr_holder = [];
        this.beat_class = [];
    }

    /**
     * RR-i window with size 3
     * @param {int} i index of rr holder
     * @returns {Array} RR1, RR2, RR3
     */
    get_r_window(i) {
        // return [RR1, RR2, RR3]
        return [this.rr_holder[i], this.rr_holder[i+1], this.rr_holder[i+2]];
    }

    /**
     * get sample size of x duration given sampling freq
     * @param {number} duration
     * @returns {*} an element of segment
     */
    duration(duration) {
        return duration * this.freq;
    }

    /**
     * @return {boolean}
     */
    C1(window) {
        const RR1 = window[0];
        const RR2 = window[1];
        return RR2 < this.duration(0.6) && this.duration(1.8)*RR2 < RR1;
    }

    /**
     * @return {boolean}
     */
    C2(window) {
        const RR1 = window[0];
        const RR2 = window[1];
        const RR3 = window[2];
        return (RR1 < this.duration(0.7) && RR2 < this.duration(0.7) && RR3 < this.duration(0.7))
            || (RR1+RR2+RR3 < this.duration(1.7));
    }

    /**
     * @return {boolean}
     */
    C3(window) {
        const RR1 = window[0];
        const RR2 = window[1];
        const RR3 = window[2];
        return 1.15*RR2 < RR1 && 1.15*RR2 < RR3;
    }

    /**
     * @return {boolean}
     */
    C4(window) {
        const RR1 = window[0];
        const RR2 = window[1];
        const RR3 = window[2];
        return (Math.abs(RR1-RR2)) < this.duration(0.3) && (RR1 < this.duration(0.8) || RR2 < this.duration(0.8))
            && (RR3 > 1.2 * BeatClassifier.mean([RR1, RR2]));
    }

    /**
     * @return {boolean}
     */
    C5(window) {
        const RR1 = window[0];
        const RR2 = window[1];
        const RR3 = window[2];
        return (Math.abs(RR2-RR3)) < this.duration(0.3) && (RR2 < this.duration(0.8) || RR3 < this.duration(0.8))
            && (RR1 > 1.2 * BeatClassifier.mean([RR2, RR3]));
    }

    /**
     * @return {boolean}
     */
    C6(window) {
        const RR1 = window[0];
        const RR2 = window[1];
        const RR3 = window[2];
        return (this.duration(2.2) < RR2 && RR2 < this.duration(3.0))
            && (Math.abs(RR1-RR2) < this.duration(0.2) || Math.abs(RR2-RR3) < this.duration(0.2));
    }

    /**
     * a support mean function
     * @param {number[]} numbers
     * @returns {number}
     */
    static mean(numbers) {
        // mean of [3, 5, 4, 4, 1, 1, 2, 3] is 2.875
        let total = 0;
        for (let i = 0; i < numbers.length; i ++) {
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
    detect(segment) {
        // add new window to process holder
        Array.prototype.push.apply(this.rr_holder, segment);
        let segmentLength = this.rr_holder.length;

        // fill as prior normal (category 1)
        this.beat_class = new Array(this.rr_holder.length).fill('normal');
        let category = [];

        let shouldStop = false;
        let i = 0;  // first window
        while (i < segmentLength-2 && !shouldStop) {
            // VF area
            if (this.C1(this.get_r_window(i))) {
                let pulseCount = 0;
                do {
                    category[i] = 'vf';  // set category Ventricular Flutter/Fibrillation
                    i += 1;  // move to next windows
                    pulseCount += 1;
                } while (i < segmentLength - 2 /* stop if last 2 element */
                    && this.C2(this.get_r_window(i)));
                if (i >= segmentLength - 2){
                    shouldStop = true;
                    i -= pulseCount;
                    break;
                } else if (pulseCount < 4) {
                    while (pulseCount > 0) {
                        i -= 1;
                        pulseCount -= 1;
                        category[i] = 'normal';  // set category normal
                    }
                }
            }
            if (!shouldStop &&
                (this.C3(this.get_r_window(i)) || this.C4(this.get_r_window(i)) || this.C5(this.get_r_window(i)))) {
                category[i] = 'pvc';  // set category PVC
            }
            if (!shouldStop && this.C6(this.get_r_window(i))) {
                category[i] = 'HeartBlock';  // set category Heart Block
            }
            i += 1;  // move to next windows
        }
        this.rr_holder.splice(0, i);  // slice from i - 1 to last
        return this.beat_class.splice(0, i);
    }
}

/**
 * @param {int} state
 * @param {BeatCategory} beat
 * @returns {int} next state
 */
function finiteStateAutomation(state, beat) {
    if (state === 1){  // Initial stage of the automaton.
        if (beat === BeatCategory.pvc)
            state = 2;
        else if (beat === BeatCategory.vf)
            state = 7;
        else if (beat === BeatCategory.HeartBlock)
            state = 8;
        else  // normal
            state = 1;
    } else if (state === 2){  // Possible ventricular bigeminy, trigeminy, couplet or tachycardia.
        if (beat === BeatCategory.normal)
            state = 3;
        else if (beat === BeatCategory.pvc)
            state = 5;
        else
            state = finiteStateAutomation(1, beat);
    } else if (state === 3){
        if (beat === BeatCategory.pvc)
            state = 2;
        else if (beat === BeatCategory.normal)
            state = 4;
        else
            state = finiteStateAutomation(1, beat);
    } else if (state === 4) {
        if (beat === BeatCategory.pvc)
            state = 2;
        else
            state = finiteStateAutomation(1, beat);
    } else if (state === 5){
        if (beat === BeatCategory.pvc)
            state = 6;
        else if (beat === BeatCategory.normal)
            state = 3;
        else
            state = finiteStateAutomation(1, beat);
    } else if (state === 6){
        if (beat === BeatCategory.pvc)
            state = 6;
        else if (beat === BeatCategory.vf)
            state = 7;
        else if (beat === BeatCategory.HeartBlock)
            state = 8;
        else if (beat === BeatCategory.normal)
            state = 3;
        else
            state = finiteStateAutomation(1, beat);
    } else if (state === 7){
        if (beat === BeatCategory.pvc)
            state = 2;
        else if (beat === BeatCategory.vf)
            state = 7;
        else if (beat === BeatCategory.HeartBlock)
            state = 8;
        else
            state = finiteStateAutomation(1, beat);
    } else if (state === 8){
        if (beat === BeatCategory.pvc)
            state = 2;
        else if (beat === BeatCategory.vf)
            state = 7;
        else if (beat === BeatCategory.HeartBlock)
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
    let i = 0;
    let episodes = [RhythmCategory.n];

    let nextState = 1;
    let lastState = 1;

    let vf = 0;
    let bigeminy = 0;
    let trigeminy = 0;
    let block = 0;

    while (i<segment.length){
        let beat = segment[i];
        lastState = nextState;
        nextState = finiteStateAutomation(nextState, beat);
        if (nextState === 2 && lastState === 3){
            bigeminy += 1;
            if (bigeminy === 2)
                episodes.push(RhythmCategory.b);
            trigeminy = 0;
            vf = 0;
            block = 0;
        } else if (nextState === 2 && lastState === 4){
            trigeminy += 1;
            if (trigeminy === 2)
                episodes.push(RhythmCategory.t);
            bigeminy = 0;
            vf = 0;
            block = 0;
        } else if (nextState === 5) {
            episodes.push(RhythmCategory.c);
            bigeminy = 0;
            trigeminy = 0;
            vf = 0;
            block = 0;
        } else if (nextState === 6) {
            if (lastState !== 6)
                episodes.push(RhythmCategory.vt);
            bigeminy = 0;
            trigeminy = 0;
            vf = 0;
            block = 0;
        }else if (nextState === 7){
            vf += 1;
            if (vf === 3)
                episodes.push(RhythmCategory.vfl);
            bigeminy = 0;
            trigeminy = 0;
            block = 0;
        }else if (nextState === 8){
            block += 1;
            if (block === 2)
                episodes.push(RhythmCategory.BII);
            bigeminy = 0;
            trigeminy = 0;
            vf = 0;
        }
        i += 1;
    }
    return episodes;
}

module.exports = {
    beatCategory: BeatCategory,
    episodeCategory: RhythmCategory,
    BeatClassifier: BeatClassifier,
    classifyEpisode: analyzeBeatClassification
};
