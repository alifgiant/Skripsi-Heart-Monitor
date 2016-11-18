var express = require('express');
var router = express.Router();
var Patient = require('../models/patient');
var Doctor = require('../models/doctor');
var passport = require('passport');

/* GET users listing. */
router.get('/', function(req, res, next) {
    res.send('respond with a resource');
});

router.post('/register-patient', function (req, res, next) {
    Patient.register(new Patient(
        {
            username : req.body.username,
            full_name : req.body.name,
            address : req.body.address,
            phone_number: req.body.phone_number,
            emergency_phone: req.body.emergency_phone,
            age : req.body.age,
            gender : req.body.gender,
            device_id: req.body.device_id
        }), req.body.password, function(err, account) {
        if (err) {
            console.log(err);
            return res.json(err);
        }else {
            passport.authenticate('global')(req, res, function () {
                Patient.findOne({username: req.body.username}, function (err, data) {
                    return res.json(data);
                })
            });
        }
    });
});

router.post('/register-doctor', function (req, res, next) {
    Doctor.register(new Doctor(
        {
            username : req.body.username,
            full_name : req.body.name,
            address : req.body.address
        }), req.body.password, function(err, account) {
        if (err) {
            console.log(err);
            return res.json(err);
        }else {
            passport.authenticate('global')(req, res, function () {
                Doctor.findOne({username: req.body.username}, function (err, data) {
                    console.log("success");
                    return res.json(data);
                })
            });
        }
    });
});

router.post('/register-doctor', function (req, res, next) {
    Doctor.register(new Doctor(
        {
            username : req.body.username,
            full_name : req.body.name,
            address : req.body.address
        }), req.body.password, function(err, account) {
        if (err) {
            console.log(err);
            return res.json(err);
        }else {
            passport.authenticate('global')(req, res, function () {
                Doctor.findOne({username: req.body.username}, function (err, data) {
                    console.log("success");
                    return res.json(data);
                })
            });
        }
    });
});

module.exports = router;
