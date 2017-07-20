/**
 * Created by maakbar on 5/29/17.
 */
let express = require('express');
let router = express.Router();
let Doctor = require('../models/doctor');
let Patient = require('../models/patient');
let utils = require('./utils');
let passport = require('passport');

router.get('/', utils.isAuthenticated, (req, res) => {
    res.render('component/body', {
        title: 'JANTUNG',
        sub:'App',
        username:req.user.username,
        full_name:req.user.full_name
    });
});

router.get('/content', (req, res, next) => {    
    // console.log('doctor', req.query.user);
    Doctor.findOne({username: req.query.user}).then((doctor) =>{
        if (doctor){
            const patients = doctor.patients;            
            res.render('menu/dashboard', {
                total: patients.length
            });
        }
    });
});

router.get('/patient/list', utils.isAuthenticated, (req, res) => {
    Doctor.findOne({username: req.query.user}).then((doctor) =>{
        if (doctor){
            const patients = doctor.patients;
            res.render('menu/list_patient', {
                patients: patients
            });
        }
    });
});

router.get('/patient/monitoring', utils.isAuthenticated, (req, res) => {    
    const patient_id = req.query.id;

    Patient.findOne({_id: patient_id.toString()}).then((patient) => {        
        if (patient)
            res.render('menu/monitoring', {patient: patient});
    });        
});

router.get('/patient/add', utils.isAuthenticated, (req, res) => {
    res.render('menu/add_patient');
});

router.get('/record', utils.isAuthenticated, (req, res) => {    
    res.render('menu/record');
});

module.exports = router;