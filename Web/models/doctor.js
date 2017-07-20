let mongoose = require('mongoose');
let Schema = mongoose.Schema;

let uniqueValidator = require('mongoose-unique-validator');
let passportLocalMongoose = require('passport-local-mongoose');

let Doctor = new Schema({
    username: { type: String, unique: true },
    full_name: String,
    patients : [{
        _id: false,
        id: {type: Schema.Types.ObjectId, ref: 'Patient'},
        full_name: String,
        device_id: String,
        phone_num: String}], //single reference to a patient\
    address: String
    // password: String auth handling with passport-local
});

Doctor.plugin(uniqueValidator);
Doctor.plugin(passportLocalMongoose);

module.exports = mongoose.model('Doctor', Doctor);

// Doctor
// - username
// - full_name
// - list patient -> one to many to Patient
// - address