#include <ESP8266WiFi.h>
#include <PubSubClient.h>

const char* sensor_id = "1";

WiFiClient espClient;
PubSubClient client(espClient);
const char* ssid = "Motion Laboratory";
const char* password = "motionf206";

// const char* mqtt_server = "192.168.137.1"; //my pc IP
const char* mqtt_server = "tcp://10.5.13.29"; //my pc IP
const int mqtt_port = 1883;

void setup_connection() {
	setup_wifi();
	connect_mqtt();
}

void setup_wifi() {
	delay(10);

	Serial.print("Connecting to ");
	Serial.println(ssid);
	WiFi.begin(ssid, password);
	while (WiFi.status() != WL_CONNECTED) {
		delay(500);
		Serial.print(".");
	}

	Serial.println("");
	Serial.println("WiFi connected");
	Serial.println("IP address: ");
	Serial.println(WiFi.localIP());
}

void connect_mqtt() {
	Serial.println("MQTT connection");
	client.setServer(mqtt_server, 1883);
}

void reconnect() {
// Loop until we're reconnected
	while (!client.connected()) {
		Serial.print("Attempting MQTT connection...");
		// Attempt to connect
		if (client.connect("ESP8266Client")) {
			Serial.println("connected");
			// Once connected, publish an announcement...
			client.publish("outTopic", "hello world");
			//Serial.println("setup read");
			//setup_ticker();
			//Serial.println("after setup");
		} else {
			Serial.print("failed, rc=");
			Serial.print(client.state());
			Serial.println(" try again in 5 seconds");
			// Wait 5 seconds before retrying
			delay(5000);
		}
	}
}

void send_message(int data) {
	Serial.println("ada");
	if (client.connected()) {
		Serial.println("kirim");
		//client.publish(sensor_id, "tes1");
		char message_buff [30];
		String pubString = "{\"report\":{\"signal\": \"" + String(data) + "\"}}";
		pubString.toCharArray(message_buff, 30);
		//Serial.println(pubString);
		//sprintf (message_buff, "%03i", data);
		client.publish("outTopic", message_buff);
	}
}

void connection_loop() {
	if (!client.connected()) {		
		reconnect();
	}else{
		//send_message();
	}
}

