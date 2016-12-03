import paho.mqtt.client as mqtt
import threading
import time
import numpy as np
import random

from PanTom import Detector
from Classifier import Classifier, BeatCategory, RhythmCategory

CLIENT_ID = 'process_py'
client_process_holder = {}


def sender_service(client, holder, topic, sensor):
    i = 0
    while True:
        source = holder['data'][topic]
        if len (source) > 0:
            delay = holder['delay'][topic]  
            i += 1
            data = source.pop()          
            if topic != 'visual' or i == 33:
                i = 0
                client.publish(str(sensor+'/'+topic), str(data), retain=True)
                # print 'send', topic, data
            time.sleep(delay)  # in ms


def sender(client, topic, messages):
    print 'processing', topic
    i = 0
    for data in messages:
        client.publish(topic, str(data), retain=True)
        time.sleep(0.05)


def on_connect(client, userdata, flags, rc):
    print "Process connected with result code " + str(rc)
    # client.subscribe(topic="stream/#", qos=1) #Gotta Catch Them All


def setupAlert(category, alert):
    # print category, alert['name'], type(alert)
    if category=='Beat':
        if alert['name']==BeatCategory.pvc['name']:
            return '%s got PVC beat#not very serious#0'
        elif alert['name']==BeatCategory.vf['name']:
            return '%s got VF beat#You may worry#1'
        elif alert['name']==BeatCategory.HeartBlock['name']:
            return '%s got Heart Block beat#You may worry#1'
    elif category=='Rhythm':
        if alert['name']==RhythmCategory.b['name']:
            return '%s got PVC bigeminy#not very serious#0'
        elif alert['name']==RhythmCategory.c['name']:
            return '%s got PVC bigeminy#not very serious#0'
        elif alert['name']==RhythmCategory.t['name']:
            return '%s got PVC trgeminy#You may worry#1'
        elif alert['name']==RhythmCategory.vt['name']:
            return '%s got Ventricular Tachycardia#You may worry#1'
        elif alert['name']==RhythmCategory.vfl['name']:
            return '%s got Ventricular Flutter/fibrillation#You need to contact immediately!#2'
        elif alert['name']==RhythmCategory.BII['name']:
            return '%s got Heart Block beat#You need to contact immediately!#2'
    
    good = ['%s is doing Great', '%s is fine', '%s got nothing to worry']
    return random.choice(good)+'#nothing happened#0'


def on_message(client, userdata, msg):
    # print msg.topic + " " + str(msg.payload)
    sensor_id = str(msg.topic).split('/')[0]
    if sensor_id not in client_process_holder:
        client_process_holder[sensor_id] = {'detector': Detector(), 'data': {
        'visual':[],
        'alert':[]}, 'delay':{'visual':0.003, 'alert':5}}
        threading.Thread(target=sender_service, args=(client, client_process_holder[sensor_id], 'visual', sensor_id)).start()
        threading.Thread(target=sender_service, args=(client, client_process_holder[sensor_id], 'alert', sensor_id)).start()

    result = client_process_holder[sensor_id]['detector'].add_data(float(msg.payload))
    # print 'incoming', float(msg.payload), result
    # global i
    # print 'incoming', i
    # i+=1

    if result[0] != 'filling':
        print 'processing bpm'
        bpm, filtered, rr_segment = result
        client.publish(sensor_id+'/bpm', str(np.mean(bpm)), retain=True)
        beat_segment = Classifier.analyze_beat_segment(rr_segment)
        episode_segment = Classifier.analyze_beat_classification(beat_segment)

        # alerts = [x['name'] for x in episode_segment if x['name'] not in ['Normal']]  # filter only non normal
        alerts = [setupAlert('Beat', x) for x in beat_segment]  # filter only non normal
        alerts = alerts + [setupAlert('Rhythm', x) for x in episode_segment]  # filter only non normal

        # print alerts
        # if len(alerts) > 1:
        #     print alerts
        #     raise Exception('ada alert')

        temp = client_process_holder[sensor_id]['data']['visual']
        client_process_holder[sensor_id]['data']['visual'] = temp + filtered
        temp = client_process_holder[sensor_id]['data']['alert']
        client_process_holder[sensor_id]['data']['alert'] = temp + alerts
        # threading.Thread(target=sender, args=(client, sensor_id+'/visual', filtered)).start()
        # threading.Thread(target=sender, args=(client, sensor_id + '/alert1', alerts)).start()
        # output sample data
    # print 'incoming', msg.payload


def run_client():
    client = mqtt.Client(client_id=CLIENT_ID)
    # client.connect("192.168.43.12", 1883, 60)
    client.connect("localhost", 1883, 60)

    client.on_connect = on_connect
    client.on_message = on_message

    client.subscribe("+/sensor")
    client.loop_forever()


### run it with python interpreter
if __name__ == "__main__":
    run_client()
