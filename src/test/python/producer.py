import json
import time
from datetime import datetime

from kafka import KafkaProducer

from models import Message, PayloadPosition, MetaData

producer = KafkaProducer(bootstrap_servers='localhost:9092', value_serializer=lambda v: v.json().encode('utf-8'))


def send_position(x, y, robot):
    data = Message(
        payload=PayloadPosition(x=x, y=y, theta=0, linear_velocity=1, angular_velocity=1),
        metadata=MetaData(mapping=f"robot-{robot}", source="positions", type="turtlesim/msg/Pose")
    )
    producer.send('in_1', key=f"robot-{robot}".encode("utf-8"), value=data, headers=[("type", b"position")])
    print(f"Sent position {robot=} {x=} {y=} now={datetime.now()}")


def send_positions(x1, y1, x2, y2):
    send_position(x1, y1, 1)
    send_position(x2, y2, 2)

time.sleep(1)
print("Start sending")
for i in range(3):
    print(f"Round: {i}")
    send_positions(10, 10, 20, 20)
    time.sleep(5)
    send_positions(10, 10, 10, 11.5)
    time.sleep(5)
    send_positions(10, 10, 10, 10.5)
    time.sleep(5)

producer.flush()
