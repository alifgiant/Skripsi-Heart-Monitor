// config/database.js
module.exports = {
    // looks like mongodb://<user>:<pass>@<domain>:27017/<db name>
    'base_url' : 'mongodb://localhost:27017/new_ta',
    'user_collections' : 'users',
    'mqtt_collections' : 'mqtt'
};