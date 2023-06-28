# kafka-streams-turtlebot4

## Dependencies

1. Install Java 17

2. Specify host IP in `HOST_IP` environment variable: this will allow ROS to communicate with the Kafka broker.

```shell script
export HOST_IP=<host_ip>
```

3. Create instance of Confluent Kafka and Confluent Schema Registry using Docker Compose:

```shell script
docker compose up
```

## Usage

Before starting the application, make sure to create mappings in [roskafka](https://gitlab.informatik.hs-furtwangen.de/ss23-forschungsprojekt-7/roskafka) first to upload generated schemas based on ROS message types to the schema registry.

```shell script
# download avro schemas
./mvnw io.confluent:kafka-schema-registry-maven-plugin:7.4.0:download
# build java classes from avro schemas
./mvnw compile
# run application
./mvnw compile quarkus:dev
```

Useful commands:

```shell
# After updating the docker-compose.yml
docker compose up -d

# Stopping all containers and DELETING all data (careful)
docker compose down -v
```

To interact with kafka, open a shell inside the kafka container or do both in one:

```shell
docker compose exec -it broker /bin/bash
kafka-topics --bootstrap-server localhost:9092 --list

# Or in one line
docker compose exec -it broker kafka-topics --bootstrap-server localhost:9092 --list
```

Topics can be created using the `create_kafka_topics.sh` script:

```shell
/bin/bash create_kafka_topics.sh
```
