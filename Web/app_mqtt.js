/**
 * Created by MuhammadAlif on 10/23/2016.
 */
var EventEmitter = require('events').EventEmitter;
var emitter = new EventEmitter();

var app_mqtt = function(packet, client) {
    /* packet sended */
    console.log('MQTT: Published topic', packet.topic);
    console.log('MQTT: Published payload', packet.payload.toString('ascii'));

    var rootTopic = packet.topic.split('/');
    emitter.emit(rootTopic[0], rootTopic[1], packet.payload.toString('ascii'));  // rootTopic[0] = tipe publish, [1] = id
};

emitter.on('sensor', function (sensorId, data) {
    console.log('get sensor data '+sensorId, data);
});

emitter.on('phone', function (userId, data) {
    var obj = JSON.parse(data);
    console.log('get phone data '+userId, obj);
});

emitter.on('tes', function (userId, data) {
    console.log('ini tes', app_mqtt.tass);
});

module.exports = app_mqtt;