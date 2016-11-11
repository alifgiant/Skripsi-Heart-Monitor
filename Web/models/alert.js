/**
 * Created by g40 on 30/07/16.
 */

var mongoose = require('mongoose');
var uniqueValidator = require('mongoose-unique-validator');
var Schema = mongoose.Schema;

var Alert = new Schema({
    alert_id: { type: 'string', unique: true },
    date: { type: Date, default: Date.now },
    _patient: { type: Schema.Types.ObjectId, ref: 'Patient' },
    detail: String,
    status: Boolean
});

Alert.plugin(uniqueValidator);

module.exports = mongoose.model('Alert', Alert);

// Alert
// - alert_id
// - username
// - date
// - status_alert
// - detail