class Connector {
private:
  void connectWifi();
  void connectMqtt();
  void subscribeMQTT();
  void resetConnection();

public:
  static const char * MQTT_BROKER;
  static const char * SENSOR_ID;
  static const int MQTT_PORT;
  static const char * OUT_TOPIC;
  static const char * IN_TOPIC;
  void publish(char* message);
  void setupConnection();
  void loop();
} ;
