/**
 * Created by maakbar on 11/18/16.
 */

// load all the things we need
let LocalStrategy   = require('passport-local').Strategy;

// load up the user model
let Doctor = require('../models/doctor');
let Patient = require('../models/patient');

function configPassport(passport) {
    // =========================================================================
    // passport session setup ==================================================
    // =========================================================================
    // required for persistent login sessions
    // passport needs ability to serialize and deserialize users out of session
    passport.serializeUser(Doctor.serializeUser());
    passport.deserializeUser(Doctor.deserializeUser());

    passport.serializeUser(Patient.serializeUser());
    passport.deserializeUser(Patient.deserializeUser());

    passport.use('sign-in-doctor', new LocalStrategy(Doctor.authenticate()));
    passport.use('sign-in-patient', new LocalStrategy(Patient.authenticate()));
}

// expose this function to our app using module.exports
module.exports = configPassport;