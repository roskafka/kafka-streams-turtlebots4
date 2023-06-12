package de.hfu;

import hfu.avro.serialisation.Diagnostics;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

@ApplicationScoped
public class Ex {

    private static final String INPUT_TOPIC = "in_1";
    private static final String OUTPUT_TOPIC = "out_1";

    private static final Logger logger = LoggerFactory.getLogger(Ex.class);

    // value of quarkus.kafka-streams.schema-registry-url
    @ConfigProperty(name = "quarkus.kafka-streams.schema-registry-url")
    String schemaRegistryUrl;

    @Produces
    public Topology createPositionsStream() {

        StreamsBuilder builder = new StreamsBuilder();

        Serde<Diagnostics> greetingSerde = new SpecificAvroSerde<>();

        final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url", schemaRegistryUrl);
        greetingSerde.configure(serdeConfig, false);

        builder.stream(INPUT_TOPIC, Consumed.with(Serdes.String(), greetingSerde))
                .peek((key, value) -> logger.info("key: {}, value: {}", key, value))
                .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), greetingSerde));

        return builder.build();
    }
}
