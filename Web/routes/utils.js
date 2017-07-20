/**
 * Created by maakbar on 5/29/17.
 */

function isAuthenticated(req, res, next) {
    // CHECK THE USER STORED IN SESSION FOR A CUSTOM VARIABLE
    if (req.isAuthenticated())
        return next();

    // IF A USER ISN'T LOGGED IN, THEN REDIRECT THEM to /
    res.redirect('/');
}

module.exports = {
    isAuthenticated: isAuthenticated
};
