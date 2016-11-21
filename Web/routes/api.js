/**
 * Created by MuhammadAlif on 10/22/2016.
 */
var express = require('express');
var router = express.Router();
var Patient = require('../models/patient');
var Doctor = require('../models/doctor');
var Device = require('../models/device');
var passport = require('passport');

/* POST user data. */
router.get('/', function(req, res, next) {
    // res.render('index', { title: 'Express' });
    res.send('respond with a resource');
});

router.post('/:user/register', function (req, res, next) {
    if (req.params.user == 'doctor'){
        Doctor.register(new Doctor({
            username : req.body.username,
            full_name : req.body.full_name,
            address : req.body.address
        }), req.body.password, function(err, account) {
            if (err) {
                res.status(err.status || 422);
                return res.json(err);
            }else{
                return res.json({"status":"success", username: req.body.username});
            }
        });
    }else if (req.params.user == 'patient'){
        Device.findOne({device_id: req.body.device_id}, function (err, device) {
            if (device){
                Patient.register(new Patient({
                    username : req.body.username,
                    full_name : req.body.full_name,
                    address : req.body.address,
                    my_phone: req.body.my_phone,
                    emergency_phone: req.body.emergency_phone,
                    age : req.body.age,
                    is_male : req.body.is_male,
                    // device_id: device.id
                    device_id: req.body.device_id
                }), req.body.password, function(err, account) {
                    if (err) {
                        res.status(err.status || 422);
                        return res.json(err);
                    }else{
                        return res.json({"status":"success", username: req.body.username});
                    }
                });
            }else {
                res.status(422);
                return res.json({"status":"failed", info: 'no device'});
            }
        });
    }else {
        res.status(400);
        return res.send({status:'failed', info:'wrong user type'});
    }
});  // Tested

router.post('/:user/login', function(req, res, next) {
    if (req.params.user == 'doctor'){
        var strategy = 'sign-in-doctor';
    }else if (req.params.user == 'patient'){
        strategy = 'sign-in-patient';
    }else {
        res.status(400);
        return res.send({status:'failed', info:'wrong user type'});
    }
    passport.authenticate(strategy, function(err, user) {
        if (err) { return next(err); }
        else if (!user) {
            res.status(401);
            if (strategy == 'sign-in-doctor'){  // doctor
                Doctor.findOne({username: req.body.username}, function (err, data) {
                    if(!data){
                        return res.send({status:'failed', info:'username'});
                    }else {
                        return res.send({status:'failed', info:'password'});
                    }
                })
            }else{ // patient
                Patient.findOne({username: req.body.username}, function (err, data) {
                    if(!data){
                        return res.send({status:'failed', info:'username'});
                    }else {
                        return res.send({status:'failed', info:'password'});
                    }
                })
            }
        }
        else {
            req.logIn(user, function (err) {
                if (err) {
                    res.status(err.status || 503);
                    return next(err);
                }else if (strategy == 'sign-in-doctor'){
                    // return user info
                    Doctor.findOne({username: req.body.username}, function (err, data) {
                        return res.json({type: 'doctor', user: data});
                    })
                }else if (strategy == 'sign-in-patient'){
                    // return user info
                    Patient.findOne({username: req.body.username}, function (err, data) {
                        return res.json({type: 'patient', data: data});
                    })
                }
            });
        }
    })(req, res, next);
}); // Tested

router.post('/device/add', function (req, res, next) {
    var newDevice = new Device({
        device_id : req.body.device_id
    });
    newDevice.save(function (err, newDevice) {
        if (err) {
            return res.status(422).send({status:'failed', message: err.errors.device_id.message});
        }
        return res.send({status:'success', device_id: newDevice.device_id});
    });
});  // Tested

router.get('/:user/:username/data/simple', function (req, res, next) {
    if (req.params.user == 'patient'){
        Patient.findOne({username: req.params.username}, function (err, patient) {
            if (patient) res.json({
                full_name:patient.full_name,
                address:patient.address,
                phone:patient.my_phone,
                is_male:patient.is_male,
                age:patient.age,
                device_id:patient.device_id
            });
            else res.status(401).send({status:'failed', info:'user not found'});
        });
    }else {
        res.status(400);
        return res.send({status:'failed', info:'wrong user type'});
    }
});  // tested patient

router.get('/:user/:username/data', function (req, res, next) {
    if (req.params.user == 'doctor'){
        Doctor.findOne({username: req.params.username}, function (err, doctor) {
            if (doctor) res.json(doctor);
            else res.status(401).send({status:'failed', info:'user not found'});
        });
    }else if (req.params.user == 'patient'){
        Patient.findOne({username: req.params.username}, function (err, patient) {
            if (patient) res.json(patient);
            else res.status(401).send({status:'failed', info:'user not found'});
        });
    }else {
        res.status(400);
        return res.send({status:'failed', info:'wrong user type'});
    }
});  // tested patient

router.post('/:user/:username/data/add', function (req, res, next) {
    if (req.params.user == 'doctor'){
        Doctor.findOne({username: req.params.username}, function (err, doctor) {
            if (doctor) res.json(doctor);
            else res.status(401).send({status:'failed', info:'user not found'});
        });
    }else if (req.params.user == 'patient'){
        Patient.findOne({username: req.params.username}, function (err, patient) {
            if (patient) {
                // res.json(patient);
                Patient.findOne({username:req.body.username}, function (err, friend) {
                    if (friend){
                        var duplicate = false;
                        for (var i = 0; i<patient.friends.length; i++){
                            if (patient.friends[i].id == friend.id){
                                patient.friends[i].name = friend.username;
                                patient.friends[i].device_id= friend.device_id;
                                duplicate = true;
                                break;
                            }
                        }
                        if (!duplicate) {
                            patient.friends.push({
                                id: friend.id,
                                name: friend.username,
                                is_male: friend.is_male,
                                device_id: friend.device_id
                            });
                        }
                        patient.save(function (err) {
                            if (!err && !duplicate) res.send({status:'success', info:'friend added',
                                name: friend.username,
                                is_male: friend.is_male,
                                device_id: friend.device_id});
                            else if (duplicate) res.send({status:'success', info:'friend updated',
                                name: friend.username,
                                is_male: friend.is_male,
                                device_id: friend.device_id});
                            else res.send(err);
                        });
                    }else res.status(401).send({status:'failed', info:'user not found'});
                });
            }
            else res.status(401).send({status:'failed', info:'user not found'});
        });
    }else {
        res.status(400);
        return res.send({status:'failed', info:'wrong user type'});
    }
});  // tested patient

module.exports = router;