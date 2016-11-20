/**
 * Created by maakbar on 11/20/16.
 */

var holder = [];

function runAlgorithm(deviceId, data, callback) {
    process.nextTick(function () {
        holder.push({id: deviceId, data: data});
        // console.log(holder);
        callback(data);
    });
}

module.exports = runAlgorithm;