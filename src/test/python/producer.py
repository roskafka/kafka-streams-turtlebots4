import time
from datetime import datetime

from confluent_kafka import Producer
from confluent_kafka.schema_registry.avro import AvroSerializer
from confluent_kafka.schema_registry import SchemaRegistryClient
from confluent_kafka.serialization import StringSerializer, SerializationContext, MessageField


bootstrap_servers = "141.28.73.94:9092"
bootstrap_servers = "localhost:9092"
schema_registry_url = "http://141.28.73.94:8081"
schema_registry_url = "http://localhost:8081"

sr = SchemaRegistryClient({"url": schema_registry_url})

greetings_schema_value = sr.get_latest_version("in_1-value")

value_serializer = AvroSerializer(schema_registry_client=sr, schema_str=greetings_schema_value.schema)
key_serializer = StringSerializer("utf_8")

producer = Producer({"bootstrap.servers": bootstrap_servers})


def send_position(x, y, robot):
    # serialize avro Greetings
    data = {
        "value": "Ich mag keine Bananen",
    }
    topic = "in_1"
    producer.produce(topic=topic,
                     key=key_serializer("rob stark"),
                     value=value_serializer(data, SerializationContext(topic, MessageField.VALUE)))
    print(f"Sent position {robot=} {x=} {y=} now={datetime.now()}")


def send_positions(x1, y1, x2, y2):
    send_position(x1, y1, 1)
    send_position(x2, y2, 2)

send_positions(10, 10, 20, 20)
producer.flush()
exit(0)
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
