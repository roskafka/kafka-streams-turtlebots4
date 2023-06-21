# kafka-streams-turtlebot4

## Dependencies

1. Adjust broker address in `KAFKA_ADVERTISED_LISTENERS` in docker-compose.yml
2. Create instance of Confluent Kafka and Confluent Schema Registry using Docker Compose:

```shell script
docker compose up
```

## Usage

```shell script
# download avro schemas
./mvnw io.confluent:kafka-schema-registry-maven-plugin:7.4.0:download
# build java classes from avro schemas
./mvnw compile
# run application
./mvnw compile quarkus:dev
```
