#!/usr/bin/python3
import requests
import time
import random

tmp_sensor = "{{\"sensor_name\" : \"LM35DZ\",\"type\" : 0,\"value\" : {}}}"
hum_sensor = "{{\"sensor_name\" : \"DHT11\",\"type\" : 1,\"value\" : {}}}"
win_1_sensor = "{\"sensor_name\" : \"WINDOW_LAB\",\"type\" : 2,\"value\" : 1}"
win_2_sensor = "{\"sensor_name\" : \"WINDOW_SOC\",\"type\" : 2,\"value\" : 0}"

url = 'http://localhost:8080/rest/sensor'
headers = {'Accept' : 'application/json', 'Content-Type' : 'application/json'}
while(True):
    x = random.uniform(0,50)
    r = requests.put(url, data=tmp_sensor.format(x), headers=headers)
    x = random.uniform(40,70)
    r = requests.put(url, data=hum_sensor.format(x), headers=headers)
    
    r = requests.put(url, data=win_1_sensor, headers=headers)
    r = requests.put(url, data=win_2_sensor, headers=headers)
    time.sleep(5)
