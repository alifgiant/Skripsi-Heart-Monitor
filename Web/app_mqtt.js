/**
 * Created by MuhammadAlif on 10/23/2016.
 */
'use strict';

let EventEmitter = require('events').EventEmitter;
let detector = require('./detection/algorithm');

class AlgorithmCallBack{
    constructor(sensorId, broker, io){
        this.sensorId = sensorId;
        this.broker = broker;
        this.io = io;    
    }

    createMessage(topic, data){
        return {
            topic: this.sensorId+ '/' + topic,
            payload: data.toString(), // or a Buffer
            // payload: filtered, // or a Buffer
            qos: 0, // 0, 1, or 2
            retain: false // or true
        };
    }

    filteredCallback(filtered) {
        let message = this.createMessage('filtered', filtered);

        // continue the filtered message to all subscribers
        // console.log('forward filtered:'+ this.sensorId, filtered);
        
        this.broker.publish(message);
        this.io.emit(message.topic, message.payload);
    }

    bpmCallback(bpm){
        let message = this.createMessage('bpm', bpm);

        // continue the bpm message to all subscribers
        // console.log('forward bpm:'+ this.sensorId, bpm);
        
        this.broker.publish(message);
        this.io.emit(message.topic, message.payload);
    }
}

class MqttApp{
    constructor(broker, socket){
        this.broker = broker;
        this.io = socket;        

        // event emitter
        this.emitter = new EventEmitter();        
        this.emitter.on('sensor', (sensorId, data) => {
            this.sensorEvent(sensorId, data);
        });
        this.emitter.on('phone', (userId, data) => {
            this.phoneEvent(userId, data);
        });    
    }

    sensorEvent(sensorId, data) {
        // console.log('get sensor read:'+sensorId, data);
        detector(sensorId, data, new AlgorithmCallBack(sensorId, this.broker, this.io));
    }

    phoneEvent (userId, data) {
        let obj = JSON.parse(data);
        // console.log('get phone data '+userId, obj);
    }

    receivedDataCallBack (packet, client) {
        /* packet received */
        // console.log('MQTT: Published topic', packet.topic);
        // console.log('MQTT: Published payload', packet.payload.toString('ascii'));
        // rootTopic: [0] = tipe publish, [1] = id
        let rootTopic = packet.topic.split('/');    

        // emit event
        this.emitter.emit(rootTopic[1], rootTopic[0], packet.payload.toString());        
    }
}

module.exports = MqttApp;
