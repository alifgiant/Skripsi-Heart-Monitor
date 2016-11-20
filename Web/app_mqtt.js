/**
 * Created by MuhammadAlif on 10/23/2016.
 */
var EventEmitter = require('events').EventEmitter;
var detector = require('./detection/algorithm');

var emitter = new EventEmitter();

var app = function (broker) {
    this.receivedDataCallBack = function(packet, client) {
        /* packet received */
        console.log('MQTT: Published topic', packet.topic);
        console.log('MQTT: Published payload', packet.payload.toString('ascii'));

        var rootTopic = packet.topic.split('/');
        emitter.emit(rootTopic[0], rootTopic[1], packet.payload.toString('ascii'), broker);  // rootTopic[0] = tipe publish, [1] = id
    };
};

emitter.on('sensor', function (sensorId, data, broker) {
    console.log('get sensor read:'+sensorId, data);
    detector(sensorId, data, function (result) {
        var message = {
            topic: 'processed/'+sensorId,
            payload: data, // or a Buffer
            qos: 0, // 0, 1, or 2
            retain: false // or true
        };
        console.log('forward processed read:'+sensorId, data);
        broker.publish(message);
    });
});

emitter.on('phone', function (userId, data) {
    var obj = JSON.parse(data);
    console.log('get phone data '+userId, obj);
});

emitter.on('tes', function (userId, data, broker) {
    console.log('ini tes', broker.clients);
});

module.exports = app;