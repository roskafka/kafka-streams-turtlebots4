package de.hfu;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class Ex {

    private static final String INPUT_TOPIC = "roskafka-hazards";
    private static final String OUTPUT_TOPIC = "kafkaros-leds";

    private static final Logger logger = LoggerFactory.getLogger(Ex.class);

    // value of quarkus.kafka-streams.schema-registry-url
    @ConfigProperty(name = "quarkus.kafka-streams.schema-registry-url")
    String schemaRegistryUrl;

    @Produces
    public Topology createPositionsStream() {

        StreamsBuilder builder = new StreamsBuilder();

        Serde<HazardDetectionVector> greetingSerde = new SpecificAvroSerde<>();
        Serde<LightringLeds> lightringLedsSerde = new SpecificAvroSerde<>();

        final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url", schemaRegistryUrl);
        greetingSerde.configure(serdeConfig, false);
        lightringLedsSerde.configure(serdeConfig, false);

        builder.stream(INPUT_TOPIC, Consumed.with(Serdes.String(), greetingSerde))
                .filter((key, value) -> value.getDetections().size() > 0)
                .peek((key, value) -> logger.info("key: {}, value: {}", key, value))
                // all 6 leds red
                .map((key, value) -> {
                    List<LedColor> leds = new ArrayList<>();
                    for (int i = 0; i < 6; i++) {
                        leds.add(new LedColor(255, 0, 0));
                    }
                    int currentSeconds = (int) (System.currentTimeMillis() / 1000);
                    Header header = new Header(new Time(currentSeconds, 0), "0");
                    return new KeyValue<>(key, new LightringLeds(header, leds, false));
                })
                .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), lightringLedsSerde));

        return builder.build();
    }
}
