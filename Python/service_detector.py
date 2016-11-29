import paho.mqtt.client as mqtt
import threading
import time
import numpy as np

from PanTom import Detector
from Classifier import Classifier

CLIENT_ID = 'process_py'
client_process_holder = {}


def sender(client, topic, messages):
    print 'processing', topic
    for data in messages:
        client.publish(topic, str(data), retain=True)
        time.sleep(0.010)


def on_connect(client, userdata, flags, rc):
    print "Connected with result code " + str(rc)
    # client.subscribe(topic="stream/#", qos=1) #Gotta Catch Them All


def on_message(client, userdata, msg):
    # print msg.topic + " " + str(msg.payload)
    sensor_id = str(msg.topic).split('/')[0]
    if sensor_id not in client_process_holder:
        client_process_holder[sensor_id] = Detector()

    result = client_process_holder[sensor_id].add_data(float(msg.payload))

    if result[0] != 'filling':
        print 'processing bpm'
        bpm, filtered, rr_segment = result
        client.publish(sensor_id+'/bpm', str(np.mean(bpm))+' bpm', retain=True)
        beat_segment = Classifier.analyze_beat_segment(rr_segment)
        episode_segment = Classifier.analyze_beat_classification(beat_segment)

        # alerts = [x['name'] for x in episode_segment if x['name'] not in ['Normal']]  # filter only non normal
        alerts = [x['name'] for x in episode_segment]  # filter only non normal

        print alerts
        if len(alerts) > 1:
            print alerts
            raise Exception('ada alert')
        # threading.Thread(target=sender, args=(client, sensor_id+'/visual', filtered)).start()
        # threading.Thread(target=sender, args=(client, sensor_id + '/alert1', alerts)).start()
        # output sample data
    # print 'incoming', msg.payload


def run_client():
    client = mqtt.Client(client_id=CLIENT_ID)
    client.connect("localhost", 1883, 60)

    client.on_connect = on_connect
    client.on_message = on_message

    client.subscribe("+/sensor")
    client.loop_forever()


### run it with python interpreter
if __name__ == "__main__":
    run_client()