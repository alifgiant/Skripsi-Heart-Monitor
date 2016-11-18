/**
 * Created by MuhammadAlif on 10/22/2016.
 */
var express = require('express');
var router = express.Router();
var Patient = require('../models/patient');
var Doctor = require('../models/doctor');
var passport = require('passport');

/* POST user data. */
router.post('/', function(req, res, next) {
    // res.render('index', { title: 'Express' });
    res.send('respond with a resource');
});

router.post('/login', function(req, res, next) {
    console.log(req.body);
    passport.authenticate('api')(req, res, function () {
        // Patient.findOne({username: req.body.username}, function (err, data) {
        //     return res.json(data);
        // })
        res.send(req);
    });
    // passport.authenticate('local', function(err, user, info) {
    //     if (err) {
    //         console.log('disini kena error');
    //         return next(err);
    //     }
    //     if (!user) { return res.json({message : "wrong username or password"}) }
    //     req.logIn(user, function(err) {
    //         console.log('disini kena error2');
    //         if (err) {
    //             console.log('disini kena error3');
    //             return next(err);
    //         }
    //         console.log('disini kena error4');
    //         // return user info
    //         Patient.findOne({username: req.body.username}, function (err,data) {
    //             return res.json(data);
    //         })
    //     });
    // })(req, res, next);

}); // Tested

module.exports = router;