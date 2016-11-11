var mongoose = require('mongoose');
var uniqueValidator = require('mongoose-unique-validator');
var Schema = mongoose.Schema;
var passportLocalMongoose = require('passport-local-mongoose');

var Patient = new Schema({
    username: { type: 'string', unique: true },
    name: String,
    _doctor : { type: Schema.Types.ObjectId, ref: 'Doctor' }, //single reference to a doctor
    alerts : [{ type: Schema.Types.ObjectId, ref: 'Alert' }],
// password: String handled by passport
    address: String,
    device_id: String,
    phone_number: String,
    alert_phone: String,
    alert_email: String,
    age : Number
});

Patient.plugin(uniqueValidator);
Patient.plugin(passportLocalMongoose);

module.exports = mongoose.model('Patient', Patient);

// Patient
// - username
// - full_name
// - address
// - device_id
// - phone_number
// - alert_phone
// - alert_email