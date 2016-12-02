import paho.mqtt.client as mqtt
import time
import random

# CLIENT_ID = 'dummy'
CLIENT_ID = 'A001'

client = mqtt.Client(client_id=CLIENT_ID)
client.connect("localhost", 1883, 60)

counter = 1
while True:
	# read = random.randint(0,1024)
	read = random.randint(60,120)
	# read = random.randint(0,5)
	# read = random.uniform(-2,2)
	print 'published', read
	# counter += 1
	# client.publish(CLIENT_ID+"/visual", read)	
	client.publish(CLIENT_ID+"/bpm", read, retain=True)	
	time.sleep(1)

