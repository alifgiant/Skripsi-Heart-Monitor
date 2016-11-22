HeartRate-Monitor
=================

My Undergraduate Thesis Project  
Reading heart signal- Transmit to Server - Analyze on Server - Forward analized data to Web and Android Phone.

Reading Heart Signal
--------------------
Heart signal readed using [photoplethysmogram](https://en.wikipedia.org/wiki/Photoplethysmogram) (PPG) sensor placed on wrist front. PPG used in the project is a product by Joel and Yury ([Pulse Sensor](http://pulsesensor.com/))  
![Pulse Sensor](http://cdn.shopify.com/s/files/1/0100/6632/products/PulseSensorAmpedFinger-web_2.jpg?v=1348514131)

Transmit to Server 
----------------------
The sensor is controlled by an ESP8266-12E, an S.o.C which already has WiFi module embedded on it. The [sketch](https://github.com/alifgiant/HeartRate-Monitor/tree/NewTA/Sensor) writed using [Platform.io ATOM IDE](http://docs.platformio.org/en/stable/ide/atom.html).

Analyzed on Server
------------------
### Purpose
The heart rate analyzed to obtain:  
* Heart Rate
* Arrhytmhia Classification

### Method
The analizing process followed these steps:  
	1. Receive heart signal sampled at 3ms (~300 Hz)  
	2. Start filtering and feature extraction algorithm  
	3. Start Classification algorithm  
	4. Forward Heart Rate and Arrhythmia Classification to subscriber  

#### Algorithm
* Filtering and Feature extraction algorithm described on [Pan and Tompkins](http://www.robots.ox.ac.uk/~gari/teaching/cdt/A3/readings/ECG/Pan+Tompkins.pdf) algorithm. Which band pass (combined of high pass and low pass) filter and Sliding Window thresholding.
* The project use Naive Bayes classifier, trained with [UCI-Lab dataset](https://archive.ics.uci.edu/ml/datasets/Arrhythmia), using features:
	* QRS duration
 	* RR Interval
	* Age
 	* Sex

### Architecture
Server run on [Node.JS(v6.9.1)](https://nodejs.org/en/download/) using [Mongodb(v.3.2.10)](https://www.mongodb.com/) as database. The project depedency can be found on project [folder](https://github.com/alifgiant/HeartRate-Monitor/tree/NewTA/Web).  
![architecture](https://raw.githubusercontent.com/alifgiant/HeartRate-Monitor/NewTA/public/images/architecture.png)
![server](https://raw.githubusercontent.com/alifgiant/HeartRate-Monitor/NewTA/public/images/server.png)

Forwarded analyzed data
-----------------------
The result will be forwarded to [web](https://github.com/alifgiant/HeartRate-Monitor/tree/NewTA/Web) and [android phone](https://github.com/alifgiant/HeartRate-Monitor/tree/NewTA/Android/Jantung) using WebSocket and MQTT respectively.  
![web screen shoot](https://raw.githubusercontent.com/alifgiant/HeartRate-Monitor/NewTA/public/images/web.jpg)
![phone screen shoot](https://raw.githubusercontent.com/alifgiant/HeartRate-Monitor/NewTA/public/images/phone.jpg)

License
-------
	Copyright 2016 Muhammad Alif Akbar
	Telkom University, Informatics Department

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.