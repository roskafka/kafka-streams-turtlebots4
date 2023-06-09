import time
from datetime import datetime

from confluent_kafka.avro import AvroProducer
from confluent_kafka.avro.cached_schema_registry_client import CachedSchemaRegistryClient

sr = CachedSchemaRegistryClient({"url": "http://localhost:8081"})

greetings_schema_value = sr.get_latest_schema("in_1-value")[1]
greetings_schema_key = sr.get_latest_schema("in_1-key")[1]

# producer with schema-registry and avro
producer = AvroProducer({
    'bootstrap.servers': 'localhost:9092',
    'schema.registry.url': 'http://localhost:8081',
}, default_value_schema=greetings_schema_value, default_key_schema=greetings_schema_key)


def send_position(x, y, robot):
    # serialize avro Greetings
    data = {
        "name": "Klaus",
        "info": "Hello world",
        "age": 42
    }
    producer.produce(topic="in_1", key=data, value=data)
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
