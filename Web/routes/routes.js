/**
 * Created by Muhammad Alif on 05/28/2016.
 */
let express = require('express');
let router = express.Router();
let Doctor = require('../models/doctor');
let Device = require('../models/device');
let utils = require('./utils');
let passport = require('passport');

/* GET home page. */
router.get('/', (req, res) => {
    if (req.isAuthenticated())
        res.redirect('/dashboard');
    else
        res.redirect('/login');
});

/* Do login, create session */
router.get('/login', (req, res) => {
    if (req.isAuthenticated())
        res.redirect('/dashboard');
    else
        res.render('login', {title: 'JANTUNG - Doctor Login', has_error: false});
});

router.post('/login', (req, res, next) => {
    let strategy = 'sign-in-doctor';
    passport.authenticate(strategy, (err, user) => {
        if (err) { return next(err); }
        else if (!user) {
            res.status(401);
            Doctor.findOne({username: req.body.username}, (err, data) => {
                if(!data){
                    return res.render('login', { title: 'JANTUNG - Doctor Login', has_error: 'username'});
                }else {
                    return res.render('login', { title: 'JANTUNG - Doctor Login', has_error: 'password'});
                }
            })
        }
        else {
            req.logIn(user, (err) => {
                if (err) {
                    res.status(err.status || 503);
                    return next(err);
                }else {
                    // res.send(user);
                    Doctor.findOne({username: req.body.username}, (err, data) => {
                        res.redirect('/dashboard');
                        // return res.json({status:"success", type: 'doctor', username: data.username});
                    })
                }
            });
        }
    })(req, res, next);
});

/* DO logout, remove session */
router.get('/logout', utils.isAuthenticated, (req, res) => {
    req.logout();
    res.redirect('/');
});

/* GET register page. */
router.get('/register', (req, res, next) => {
    res.render('register', { title: 'JANTUNG - Doctor Registration', has_error: false });
});

router.post('/register', (req, res, next) => {
    Doctor.register(new Doctor({
        username : req.body.username,
        full_name : req.body.full_name,
        address : req.body.address
    }), req.body.password, (err, account) => {
        if (err) {
            res.status(err.status || 422);
            switch (err.name){
                case 'UserExistsError':
                    return res.render('register', { title: 'JANTUNG - Doctor Registration', has_error: true});
            }
        }else{
            return res.redirect('/login');
        }
    });
});

/* GET register page. */
router.get('/device-add', (req, res, next) => {
    res.render('device_add', { title: 'JANTUNG - Device Add', has_error: false });
});

router.post('/device-add', function (req, res, next) {
    let newDevice = new Device({
        device_id : req.body.device_id
    });
    newDevice.save(function (err, newDevice) {
        if (err) {
            return res.status(422).render('device_add', { title: 'JANTUNG - Device Add', has_error: true });
        }
        res.render('device_add', { title: 'JANTUNG - Device Add', has_error: false });
    });
});  // Tested

module.exports = router;
