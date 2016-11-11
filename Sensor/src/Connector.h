class Connector {
  static const char * DEFAULT_SSID;
  static const char * DEFAULT_PASS;
  static const char * SENSOR_ID;
  static const int MQTT_PORT;
  static const char * OUT_TOPIC;
  static const char * IN_TOPIC;
  static bool IS_CONNECTED;
private:
  void connectWifi(const char * ssid, const char * pass);
  void connectMqtt();
  void subscribeMQTT();
  void saveSSIDinfo(const char * ssid, const char * pass);
  const char * loadSSID();
  const char * loadPass();
  void resetConnection();
public:
  void publish(char message[]);
  void setupConnection();
  void loop();
} ;
