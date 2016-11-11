var express = require('express');
var router = express.Router();

/* GET home page. */
router.get('/', function(req, res, next) {
  res.render('index', { title: 'JANTUNG web apps' });
});

/* GET register page. */
router.get('/register', function(req, res, next) {
  res.render('register', { title: 'Express' });
});

/* GET register page. */
router.get('/dashboard', function(req, res, next) {
  res.render('register', { title: 'Express' });
});

module.exports = router;
