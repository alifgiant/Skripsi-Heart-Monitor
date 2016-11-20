var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var uniqueValidator = require('mongoose-unique-validator');
var passportLocalMongoose = require('passport-local-mongoose');

var Patient = new Schema({
    username: { type: String, unique: true },
    full_name: String,
    // password: String handled by passport
    // _doctor : { type: Schema.Types.ObjectId, ref: 'Doctor' }, //single reference to a doctor
    friends : [{
        _id: false,
        id: {type: Schema.Types.ObjectId, ref: 'Patient'},
        name: String,
        is_male: Boolean,
        device_id: String}], //single reference to a patient
    address: String,
    my_phone: String,
    emergency_phone: String,
    age : Number,
    is_male : Boolean,
    device_id: String
    // device_id: {type: Schema.Types.ObjectId, ref: 'Device'}
});

Patient.plugin(uniqueValidator);
Patient.plugin(passportLocalMongoose);

module.exports = mongoose.model('Patient', Patient);

// Patient
// - username
// - full_name
// - address
// - my_phone
// - emergency_phone
// - age
// - gender
// - device_id