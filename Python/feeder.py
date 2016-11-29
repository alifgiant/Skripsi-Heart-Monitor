import paho.mqtt.client as mqtt
import CsvLoader
import time

from PanTom import Detector
detector = Detector()

CLIENT_ID = 'ow0003'


def on_connect(client, userdata, flags, rc):
    print "Connected with result code " + str(rc)
    # client.subscribe(topic="stream/#", qos=1) #Gotta Catch Them All


data_csv = CsvLoader.load()
client = mqtt.Client(client_id=CLIENT_ID)
client.connect("localhost", 1883, 60)

# device_id = msg.topic.split('/')[1]
# device_id = "02WXO01"

for raw in data_csv:
    client.publish(CLIENT_ID+'/sensor', str(raw))
    print CLIENT_ID+'/sensor', str(raw)
    time.sleep(0.010)
