import json
from datetime import datetime

from confluent_kafka import Consumer
from confluent_kafka.schema_registry.avro import AvroDeserializer
from confluent_kafka.schema_registry import SchemaRegistryClient
from confluent_kafka.serialization import StringSerializer, SerializationContext, MessageField

bootstrap_servers = "141.28.73.94:9092"
schema_registry_url = "http://141.28.73.94:8081"

sr = SchemaRegistryClient({"url": schema_registry_url})
greetings_schema_value = sr.get_latest_version("in_1-value")

value_deserializer = AvroDeserializer(schema_registry_client=sr, schema_str=greetings_schema_value.schema)


consumer = Consumer({
    'bootstrap.servers': bootstrap_servers,
    'group.id': 'mygroup',
    'auto.offset.reset': 'earliest'
})
print("Waiting for messages...")
consumer.subscribe(['out_1'])

while True:
    try:
        # SIGINT can't be handled when polling, limit timeout to 1 second.
        msg = consumer.poll(1.0)
        if msg is None:
            continue

        data = value_deserializer(msg.value(), SerializationContext(msg.topic(), MessageField.VALUE))
        if data is not None:
            print(f"Received message ts={datetime.now()} {data=}")
    except KeyboardInterrupt:
        break

consumer.close()
