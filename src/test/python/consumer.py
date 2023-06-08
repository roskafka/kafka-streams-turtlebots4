import json
from datetime import datetime

from kafka import KafkaConsumer


consumer = KafkaConsumer('out_1', bootstrap_servers='localhost:9092', group_id='color_logger')
print("Waiting for messages...")
for message in consumer:
    print("Received message ts: ", datetime.now())
    print(message)
