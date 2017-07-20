#include <string>

class Connector {
private:
  const int INDEX_LIMIT = 1000;
  int index = 0;
  const char * MQTT_BROKER_ADDRESS = "192.168.43.13";  // using Protable
  // const char * MQTT_BROKER_ADDRESS  = "10.30.41.30"; // others
  const char * SENSOR_ID            = "ID001";
  const char * OUT_CHANNEL          = "sensor";
  std::string TOPIC;
  const int MQTT_PORT               = 1883;


  void connectWifi();
  void connectMqtt();
  void subscribeMQTT();
  void resetConnection();
  std::string buildMessage(int sample);

public:
  Connector();
  void publish(int sample);
  void setupConnection();
  void loop();
} ;
