import paho.mqtt.client as mqtt
import threading
import time
import numpy as np
from PanTom_old import Detector
from collections import Counter

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


def on_connect(client, userdata, flags, rc):
    print "Process connected with result code " + str(rc)
    # client.subscribe(topic="stream/#", qos=1) #Gotta Catch Them All

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
        filtered, PVC, PAC, BUNDLE_BRANCH, AtrialTachycardia, VentricularTachycardia, BundleBranchBlock, bpm = result
        client.publish(sensor_id+'/bpm', str(np.mean(bpm)), retain=True)

        # print 'result', result[1:]

        PVC_counter = Counter(PVC)
        PAC_counter = Counter(PAC)
        B_counter = Counter(BUNDLE_BRANCH)
        AT_counter = Counter(AtrialTachycardia)
        VT_counter = Counter(VentricularTachycardia)
        BB_counter = Counter(BundleBranchBlock)

        new_alert = []
        if PVC_counter[True]>(0.35*len(PVC)):
        	new_alert.append('%s got a minor issue#PVC is detected#1')
        if PAC_counter[True]>(0.55*len(PAC)):
        	new_alert.append('%s got a minor issue#PAC is detected#1')
        if B_counter[True]>(0.35*len(B_counter)):
        	new_alert.append('You should contact %s soon#Bundle Branch is detected#1')
        if AT_counter[True]>(0.35*len(AtrialTachycardia)):
        	new_alert.append('You should contact %s soon#Atrial Tachycardia is detected#1')
        if VT_counter[True]>(0.35*len(VentricularTachycardia)):
        	new_alert.append('You should contact %s immediately#Ventricular Tachycardia#2')
        if BB_counter[True]>(0.35*len(BundleBranchBlock)):
        	new_alert.append('You should contact %s soon#Bundle Branch Block is detected#1')
        
        # print 'result', result[1:]
        if len(new_alert)==0:
        	new_alert.append('%s is doing great #Nothing is detected#0')

        temp = client_process_holder[sensor_id]['data']['visual']
        client_process_holder[sensor_id]['data']['visual'] = temp + filtered
        temp = client_process_holder[sensor_id]['data']['alert']
        client_process_holder[sensor_id]['data']['alert'] = temp + new_alert
        # print 'finish adding'
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
