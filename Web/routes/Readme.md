SERVER API
==========

-------  
**base_address example: http://localhost:3000**  

-------


Login
-----
### url
```
POST
http://{base_address}/api/:user_type/login
```
user_type:
* patient
* doctor

param key, encoding: x-www-urlencoded  
* username
* password

### response
```javascript
{status:'failed', info:'wrong user type'}
or
{status:'failed', info:'username'}
or
{status:'failed', info:'password'}
or
{status:"success", type: 'patient', username: user.username}	
or
{status:"success", type: 'doctor', username: user.username}
```
user merupakan object Doctor / Patient

### error status
```	
depend on (info)
'wrong user_type' : 400
'username' : 401
'password': 401
```

Register
-----
### url
```
POST
http://{base_address}/api/:user_type/register
```
user_type:
* patient
* doctor  

doctor param key, encoding: x-www-urlencoded
* username
* full_name
* address

patient param key, encoding: x-www-urlencoded
* username
* password
* full_name
* address
* my_phone
* emergency_phone
* age
* is_male
* device_id

### response
```javascript
{status:"success", username: user.username}
or
{status:"failed", info: 'no device'}
or
{status:'failed', info:'wrong user type'}
```
user merupakan object dari Doctor / Patient

### error status
```	
depend on (info)
'wrong user_type' : 400
'no device' : 422
```
info 'no device', sended if patient input a not registered Device ID

Get User Data
-------------
### url
```
GET
http://{base_address}/api/:user_type/:username/data
```
user_type:
* patient
* doctor 

username:
username of patient or doctor

### response
```javascript
{
  "_id": "58330f117bb84354192670f7",
  "username": "akbar",
  "full_name": "muh alif akbar",
  "address": "Jl. Terserah",
  "my_phone": "62 813 12239294",
  "emergency_phone": "0411 423 925",
  "age": 21,
  "is_male": true,
  "device_id": "ow0003",
  "__v": 2,
  "friends": [
    {
      "id": "58330f1f7bb84354192670f8",
      "name": "alif",
      "is_male": true,
      "device_id": "ow0002"
    },
    {
      "id": "58330f367bb84354192670f9",
      "name": "Sarah",
      "is_male": false,
      "device_id": "ow0001"
    }
  ]
}
or
{status:'failed', info:'user not found'}
or
{status:'failed', info:'wrong user type'}
```
user merupakan object dari Doctor / Patient

### error status
```	
depend on (info)
'wrong user_type' : 400
'user not found' : 401
```

Get User Data - Simpe
---------------------
### url
```
GET
http://{base_address}/api/:user_type/:username/data/simple
```
user_type:
* patient
* doctor 

username:
username of patient or doctor

### response
```javascript
{
  "full_name": "muh alif akbar",
  "address": "Jl. Terserah",
  "phone": "62 813 12239294",
  "is_male": true,
  "age": 21,
  "device_id": "ow0003"
}
or
{status:'failed', info:'user not found'}
or
{status:'failed', info:'wrong user type'}
```
user merupakan object dari Doctor / Patient

### error status
```	
depend on (info)
'wrong user_type' : 400
'user not found' : 401
```

ADD Friend to Patient
---------------------
### url
```
POST
http://{base_address}/api/:user_type/:username/data/add
```
user_type:
* patient
* doctor

param key, encoding: x-www-urlencoded  
* username

### response
```javascript
{
  "status": "success",
  "info": "patient added",
  "name": "akbar",
  "is_male": true,
  "device_id": "ow0003"
}
or
{
  "status": "success",
  "info": "patient updated",
  "name": "akbar",
  "is_male": true,
  "device_id": "ow0003"
}
or
{
  "status": "success",
  "info": "friend added",
  "name": "akbar",
  "is_male": true,
  "device_id": "ow0003"
}
or
{
  "status": "success",
  "info": "friend updated",
  "name": "akbar",
  "is_male": true,
  "device_id": "ow0003"
}
or
{status:'failed', info:'user not found'}
or
{status:'failed', info:'wrong user type'}
```

### error status
```	
depend on (info)
'wrong user_type' : 400
'user not found' : 401
```

Remove Friend of Patient
---------------------
### url
```
POST
http://{base_address}/api/:user_type/:username/data/remove
```
user_type:
* patient
* doctor

param key, encoding: x-www-urlencoded  
* username

### response
```javascript
{ status: "success", info: "friend removed"}
or
{status: "failed", info: "friend not found"}
or
{status:'failed', info:'wrong user type'}
```

### error status
```	
depend on (info)
'wrong user_type' : 400
'friend not found' : 401
```