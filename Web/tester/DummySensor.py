import paho.mqtt.client as mqtt
import time
import random

# CLIENT_ID = 'dummy'
CLIENT_ID = 'ow0002'

client = mqtt.Client(client_id=CLIENT_ID)
client.connect("localhost", 1883, 60)

while True:
	# read = random.randint(0,1024)
	read = random.randint(60,120)
	print 'published', read
	client.publish("bpm/"+CLIENT_ID, read)	
	time.sleep(1)