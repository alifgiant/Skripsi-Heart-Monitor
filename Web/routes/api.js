/**
 * Created by MuhammadAlif on 10/22/2016.
 */
var express = require('express');
var router = express.Router();

/* POST user data. */
router.post('/', function(req, res, next) {
    // res.render('index', { title: 'Express' });
    res.send('respond with a resource');
});

module.exports = router;