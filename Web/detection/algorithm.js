/**
 * loader of algorithms
 */

let classifier = require('./classifier');
let PanTomkins = require('./PanTomkins');

let holder = {};

// const SAMPLING_FREQUENCY = 200;
const SAMPLING_FREQUENCY = 360;
const FPS = 25;  // frame rate send back filter result
const DOWN_SAMPLE_COUNT = Math.round(SAMPLING_FREQUENCY / FPS);

/**
 * @param {String} deviceId id of connected device
 * @param {float} data sensor read value
 * @param {AlgorithmCallBack} callback an instance of AlgorithmCallBack
 * @returns {Array} of category
 * category:
 * 1 = Normal
 * 2 = PVC
 * 3 = VF
 * 4 = Heart Block
 */
function runAlgorithm(deviceId, data, callback) {
    process.nextTick(function () {
        // holder.push({id: deviceId, data: data});

        if (!(deviceId in holder)){  // device id not in holder
            holder[deviceId] = {
                count: 0,
                panTom: new PanTomkins(SAMPLING_FREQUENCY),
                beatClassifier: new classifier.BeatClassifier(SAMPLING_FREQUENCY)
            };
        }

        holder[deviceId].count += 1;  //
        holder[deviceId].panTom.execute(data, (type, data) => {
            if (type === 'filter'
                && holder[deviceId].count % DOWN_SAMPLE_COUNT === 0){ // send only if sampling ok
                // console.log('filtered', data);
                callback.filteredCallback(data);
                holder[deviceId].count = 0;  // reset count, send
                
            }else if (type === 'beat') {
                // data = [this.peaks_position, this.rr_distance]
                let positions = data[0];
                const rr_distances = data[1];

                const bpm = (SAMPLING_FREQUENCY * 60) /* 1 minute = freq * 60s */ / classifier.BeatClassifier.mean(rr_distances);                
                callback.bpmCallback(bpm);

                handleRrSegment(holder[deviceId], rr_distances, callback);
            }
        });
    });
}

function handleRrSegment(processor, rrSegment, callback) {
    process.nextTick(function () {
        let beatClassification = processor.beatClassifier.detect(rrSegment);
        // let episodeClassification = classifier.classifyEpisode(beatClassification);
        // callback(beatClassification, episodeClassification);
    });
}

module.exports = runAlgorithm;

