var mongoose = require('mongoose');
var uniqueValidator = require('mongoose-unique-validator');
var Schema = mongoose.Schema;
var passportLocalMongoose = require('passport-local-mongoose');

var Doctor = new Schema({
    username: { type: 'string', unique: true },
    full_name: String,
    patients : [{ type: Schema.Types.ObjectId, ref: 'Patient'}],
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