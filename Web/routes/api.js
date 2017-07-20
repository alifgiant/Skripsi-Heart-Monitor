/**
 * Created by MuhammadAlif on 10/22/2016.
 */
let express = require('express');
let router = express.Router();
let Patient = require('../models/patient');
let Doctor = require('../models/doctor');
let Device = require('../models/device');
let passport = require('passport');

/* POST user data. */
router.get('/', function(req, res, next) {
    // res.render('index', { title: 'Express' });
    res.send('respond with a resource');
});

router.post('/device/add', function (req, res, next) {
    let newDevice = new Device({
        device_id : req.body.device_id
    });
    newDevice.save(function (err, newDevice) {
        if (err) {
            return res.status(422).send({status:'failed', message: err.errors.device_id.message});
        }
        return res.send({status:'success', device_id: newDevice.device_id});
    });
});  // Tested

router.post('/:user/login', function(req, res, next) {
    if (req.params.user === 'doctor'){
        let strategy = 'sign-in-doctor';
    }else if (req.params.user === 'patient'){
        strategy = 'sign-in-patient';
    }else {
        res.status(400);
        return res.send({status:'failed', info:'wrong user type'});
    }
    passport.authenticate(strategy, function(err, user) {
        if (err) { return next(err); }
        else if (!user) {
            res.status(401);
            if (strategy === 'sign-in-doctor'){  // doctor
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
                }else if (strategy === 'sign-in-doctor'){
                    // return user info
                    Doctor.findOne({username: req.body.username}, function (err, data) {
                        return res.json({status:"success", type: 'doctor', username: data.username});
                    })
                }else if (strategy === 'sign-in-patient'){
                    // return user info
                    Patient.findOne({username: req.body.username}, function (err, data) {
                        return res.json({status:"success", type: 'patient', username: data.username});
                    })
                }
            });
        }
    })(req, res, next);
}); // Tested

router.post('/:user_type/register', function (req, res, next) {
    if (req.params.user_type=== 'doctor'){
        Doctor.register(new Doctor({
            username : req.body.username,
            full_name : req.body.full_name,
            address : req.body.address
        }), req.body.password, function(err, account) {
            if (err) {
                res.status(err.status || 422);
                return res.json(err);
            }else{
                return res.json({status:"success", username: req.body.username});
            }
        });
    }else if (req.params.user_type === 'patient'){
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
                    device_id: req.body.device_id
                }), req.body.password, function(err, account) {
                    if (err) {
                        res.status(err.status || 422);
                        return res.json(err);
                    }else{
                        return res.json({status:"success", username: req.body.username});
                    }
                });
            }else {
                res.status(422);
                return res.json({status:"failed", info: 'no device'});
            }
        });
    }else {
        res.status(400);
        return res.send({status:'failed', info:'wrong user type'});
    }
});  // Tested

router.get('/:user_type/:username/data/simple', function (req, res, next) {
    if (req.params.user_type === 'doctor'){
        Doctor.findOne({username: req.params.username}, function (err, doctor) {
            if (doctor) res.json(doctor);
            else res.status(401).send({status:'failed', info:'user not found'});
        });
    }else if (req.params.user_type === 'patient'){
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

router.get('/:user_type/:username/data', function (req, res, next) {
    if (req.params.user_type === 'doctor'){
        Doctor.findOne({username: req.params.username}, function (err, doctor) {
            if (doctor) res.json(doctor);
            else res.status(401).send({status:'failed', info:'user not found'});
        });
    }else if (req.params.user_type === 'patient'){
        Patient.findOne({username: req.params.username}, function (err, patient) {
            if (patient) res.json(patient);
            else res.status(401).send({status:'failed', info:'user not found'});
        });
    }else {
        res.status(400);
        return res.send({status:'failed', info:'wrong user type'});
    }
});  // tested patient

router.post('/:user_type/:username/data/add', function (req, res, next) {
    if (req.params.user_type === 'doctor'){
        Doctor.findOne({username: req.params.username}, function (err, doctor) {
            if (doctor) {
                // res.json(doctor);
                Patient.findOne({username:req.body.username}, function (err, patient) {
                    if (patient){
                        let duplicate = false;
                        for (let i = 0; i<doctor.patients.length; i++){
                            if (doctor.patients[i].id.toString() === patient.id.toString()){
                                doctor.patients[i].full_name = patient.full_name;
                                doctor.patients[i].device_id = patient.device_id;
                                doctor.patients[i].phone_num = patient.my_phone;
                                duplicate = true;
                                break;
                            }
                        }
                        if (!duplicate) {
                            doctor.patients.push({
                                id: patient.id,
                                full_name: patient.full_name,
                                device_id: patient.device_id,
                                phone_num: patient.my_phone
                            });
                        }
                        doctor.save(function (err) {
                            if (!err && !duplicate) res.send({status:'success', info:'patient added',
                                name: patient.username,
                                is_male: patient.is_male,
                                device_id: patient.device_id});
                            else if (duplicate) res.send({status:'success', info:'patient updated',
                                name: patient.username,
                                is_male: patient.is_male,
                                device_id: patient.device_id});
                            else res.send(err);
                        });
                    }else res.status(200).send({status:'failed', info:'Patient not found'});
                });
            }
            else res.status(401).send({status:'failed', info:'Doctor not found'});
        });
    }else if (req.params.user_type === 'patient'){
        Patient.findOne({username: req.params.username}, function (err, patient) {
            if (patient) {
                // res.json(patient);
                Patient.findOne({username:req.body.username}, function (err, friend) {
                    if (friend){
                        let duplicate = false;
                        for (let i = 0; i<patient.friends.length; i++){
                            if (patient.friends[i].id.toString() === friend.id.toString()){
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
                    }else res.status(200).send({status:'failed', info:'user not found'});
                });
            }
            else res.status(401).send({status:'failed', info:'user not found'});
        });
    }else {
        res.status(400);
        return res.send({status:'failed', info:'wrong user type'});
    }
});  // tested patient

router.post('/:user_type/:username/data/remove', function (req, res, next) {
    if (req.params.user_type === 'doctor'){
        Doctor.findOne({username: req.params.username}, function (err, doctor) {
            if (doctor) res.json(doctor);
            else res.status(401).send({status:'failed', info:'user not found'});
        });
    }else if (req.params.user_type === 'patient'){
        Patient.findOne({username: req.params.username}, function (err, patient) {
            if (patient){
                let found = false;
                for (let i = 0; i<patient.friends.length; i++){
                    if (patient.friends[i].name === req.body.username){
                        patient.friends.splice(i, 1);
                        found = true;
                        break;
                    }
                }
                if (found){
                    patient.save(function (err) {
                        if (!err) res.send({status:'success', info:'friend removed'});
                        else res.send(err);
                    });
                }else res.status(401).send({status:'failed', info:'friend not found'});
            }
            else res.status(401).send({status:'failed', info:'user not found'});
        });
    }else {
        res.status(400);
        return res.send({status:'failed', info:'wrong user type'});
    }
});

module.exports = router;