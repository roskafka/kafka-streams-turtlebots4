# create topics in docker container
docker compose exec -it broker kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic roskafka-hazards
docker compose exec -it broker kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic kafkaros-leds