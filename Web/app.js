let express = require('express');
let path = require('path');
let favicon = require('serve-favicon');
let logger = require('morgan');
let cookieParser = require('cookie-parser');
let session = require('cookie-session');
let bodyParser = require('body-parser');
let mongoose = require('mongoose');
// Use bluebird
mongoose.Promise = require('bluebird');
let passport = require('passport');

let app = express();

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'pug');

// uncomment after placing your favicon in /public
//app.use(favicon(path.join(__dirname, 'public', 'favicon.ico')));
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(session({keys: ['jantung', 'is', 'here']}));
app.use(require('node-sass-middleware')({
  src: path.join(__dirname, 'public'),
  dest: path.join(__dirname, 'public'),
  indentedSyntax: true,
  sourceMap: true
}));
app.use(express.static(path.join(__dirname, 'public')));
app.use(express.static(path.join(__dirname, '/node_modules/admin-lte/')));
// app.use(express.static(path.join(__dirname, '/node_modules/bootstrap/dist/')));

/*USER HANDLE*/
let configPassport = require('./config/passport');
// passport middleware config
app.use(passport.initialize());
app.use(passport.session());
configPassport(passport);

/*ROUTES HANDLE*/
let routes = require('./routes/routes');
let dashboard = require('./routes/dashboard');
let api = require('./routes/api');

app.use('/', routes);
app.use('/dashboard', dashboard);
app.use('/api', api);

// mongoose
let configDatabase = require('./config/database');
mongoose.connect(configDatabase.base_url);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  let err = new Error('Not Found');
  err.status = 404;
  next(err);
});

// error handlers

// development error handler
// will print stacktrace
if (app.get('env') === 'development') {
  app.use(function(err, req, res, next) {
    res.status(err.status || 500);
    res.render('error', {
      message: err.message,
      error: err
    });
  });
}

// production error handler
// no stack traces leaked to user
app.use(function(err, req, res, next) {
  res.status(err.status || 500);
  res.render('error', {
    message: err.message,
    error: {}
  });
});

module.exports = app;
