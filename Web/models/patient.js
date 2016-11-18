var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var uniqueValidator = require('mongoose-unique-validator');
var passportLocalMongoose = require('passport-local-mongoose');

var Patient = new Schema({
    username: { type: 'string', unique: true },
    full_name: String,
    // password: String handled by passport
    // _doctor : { type: Schema.Types.ObjectId, ref: 'Doctor' }, //single reference to a doctor
    friends : [{ type: Schema.Types.ObjectId, ref: 'Patient' }],
    address: String,
    my_phone: String,
    emergency_phone: String,
    age : Number,
    gender : Boolean,
    device_id: String
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