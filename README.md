# kafka-streams-turtlebot4

## Dependencies

1. Adjust broker address in `KAFKA_ADVERTISED_LISTENERS` in docker-compose.yml
2. Create instance of Confluent Kafka and Confluent Schema Registry using Docker Compose:

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
