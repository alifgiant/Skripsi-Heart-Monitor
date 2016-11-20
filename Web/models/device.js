/**
 * Created by maakbar on 11/16/16.
 */
var mongoose = require('mongoose');
var Schema = mongoose.Schema;
var uniqueValidator = require('mongoose-unique-validator');

var Device = new Schema({
    device_id : { type: String, unique: true } // field level
});

Device.plugin(uniqueValidator);
module.exports = mongoose.model('Device', Device);