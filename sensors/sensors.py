#!/usr/bin/python3
import requests
import time
import random

tmp_sensor = "{{\"token\" : \"WuU43xmFZsYpSZXbkI6l\",\"value\" : {}}}"
hum_sensor = "{{\"token\" : \"EPWw05ahiHfBoPiQupRL\",\"value\" : {}}}"
win_1_sensor = "{\"token\" : \"fjTAqEWwW4VH1J05KxTj\",\"value\" : 1}"
win_2_sensor = "{\"token\" : \"X8jpzDpmLvCedjy2Wa6l\",\"value\" : 0}"

url = 'http://localhost:8080/rest/Sensor'
headers = {'Accept' : 'application/json', 'Content-Type' : 'application/json'}
while(True):
    x = random.uniform(0,50)
    r = requests.put(url, data=tmp_sensor.format(int(x)), headers=headers)
    x = random.uniform(40,70)
    r = requests.put(url, data=hum_sensor.format(int(x)), headers=headers)
    
    r = requests.put(url, data=win_1_sensor, headers=headers)
    r = requests.put(url, data=win_2_sensor, headers=headers)
    time.sleep(5)
