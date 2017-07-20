import paho.mqtt.client as mqtt
import CsvLoader
from time import sleep

# CLIENT_ID = '02WXO01'
CLIENT_ID = 'ID001'

# Test sample
numbers = [100]

if __name__ == '__main__':
	client = mqtt.Client(client_id=CLIENT_ID)
	client.connect("localhost", 1883, 60)
	for number in numbers:
		number = 'MIT_BIH/' + str(number)

		# raw = CsvLoader.load(number + '/record.csv')[:2935]  # 8 second
		raw = CsvLoader.load(number + '/record.csv')[:20000]  # 8 second
		for data in raw:
			client.publish(CLIENT_ID+"/sensor", data)
			sleep(0.005)
