#!/usr/bin/python3
import requests
import time
import random

url_lock = 'http://localhost:8080/rest/app/sensor/lock'
url_unlock = 'http://localhost:8080/rest/app/sensor/unlock'
user_data = {"pin" : "1234", "rfid" : "617364617364"} 

r = requests.post(url_unlock, data=user_data)
print(r)

r = requests.post(url_lock, data=user_data)
print(r)
