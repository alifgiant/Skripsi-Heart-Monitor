var express = require('express');
var router = express.Router();
var Patient = require('../models/patient');
var Doctor = require('../models/doctor');
var Device = require('../models/device');
var passport = require('passport');

/* GET home page. */
router.get('/', function(req, res, next) {
    if (req.isAuthenticated())
        res.redirect('/dashboard');
    else
        res.redirect('/login');
});

router.get('/dashboard', function(req, res, next) {
    res.render('dashboard', {title: 'JANTUNG web apps', username:'Alif Akbar'});
});

router.get('/login', function(req, res, next) {
    res.render('login', {title: 'JANTUNG web apps'});
});

router.post('/login', function(req, res, next) {
    var strategy = 'sign-in-doctor';
    passport.authenticate(strategy, function(err, user) {
        if (err) { return next(err); }
        else if (!user) {
            res.status(401);
            Doctor.findOne({username: req.body.username}, function (err, data) {
                if(!data){
                    return res.send({status:'failed', info:'username'});
                }else {
                    return res.send({status:'failed', info:'password'});
                }
            })
        }
        else {
            req.logIn(user, function (err) {
                if (err) {
                    res.status(err.status || 503);
                    return next(err);
                }else {
                    // return user info
                    Doctor.findOne({username: req.body.username}, function (err, data) {
                        res.redirect('/dashboard');
                        // return res.json({status:"success", type: 'doctor', username: data.username});
                    })
                }
            });
        }
    })(req, res, next);
});

/* GET register page. */
router.get('/register', function(req, res, next) {
    res.render('register', { title: 'Express' });
});

/* GET register page. */
router.get('/dashboard', function(req, res, next) {
    res.render('register', { title: 'Express' });
});

/* GET users listing. */
router.get('/send', function(req, res, next) {
    res.send('respond with a resource');
});


module.exports = router;
