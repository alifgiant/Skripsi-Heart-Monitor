import paho.mqtt.client as mqtt
import time
import random

CLIENT_ID = 'dummy'

client = mqtt.Client(client_id=CLIENT_ID)
client.connect("localhost", 1883, 60)

while True:
	read = random.randint(0,1024)
	client.publish("sensor/"+CLIENT_ID, read)
	print read
	time.sleep(0.003)