/**
 * loader of algorithms
 */

var classifier = require('./classifier');

var holder = [];

function runAlgorithm(deviceId, data, callback) {
    process.nextTick(function () {
        holder.push({id: deviceId, data: data});
        // console.log(holder);
        callback(data);
    });
}

function handleRrSegment(rrSegment, callback) {
    process.nextTick(function () {
        var beatClassification = classifier.classifyBeat(rrSegment);
        var episodeClassification = classifier.classifyEpisode(beatClassification);
        callback(beatClassification, episodeClassification);
    });
}

console.log(classifier.beatCategory);

module.exports = runAlgorithm;

