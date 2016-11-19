class Connector {
  static const char * DEFAULT_SSID;
  static const char * DEFAULT_PASS;
  static const char * SENSOR_ID;
  static const int MQTT_PORT;
  static const char * OUT_TOPIC;
  static const char * IN_TOPIC;
private:
  void connectWifi();
  void connectMqtt();
  void subscribeMQTT();
  void resetConnection();
public:
  void publish(char message[]);
  void setupConnection();
  void loop();
} ;
