var express = require('express');
var router = express.Router();

/* GET home page. */
router.get('/', function(req, res, next) {
    if (req.isAuthenticated())
        res.redirect('/dashboard');
    else
        res.redirect('/login');
});

router.get('/dashboard', function(req, res, next) {
    res.render('index', {title: 'JANTUNG web apps'});
});

router.get('/login', function(req, res, next) {
    res.render('login2', {title: 'JANTUNG web apps'});
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
