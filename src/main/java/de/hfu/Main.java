package de.hfu;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class Main {

    private static final String INPUT_TOPIC = "roskafka-hazards";
    private static final String INNER_TOPIC = "hazards";
    private static final String OUTPUT_TOPIC = "kafkaros-leds";

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @ConfigProperty(name = "quarkus.kafka-streams.schema-registry-url")
    String schemaRegistryUrl;

    @Produces
    public Topology createPositionsStream() {

        StreamsBuilder builder = new StreamsBuilder();

        Serde<HazardDetectionVector> hazardDetectionVectorSerde = new SpecificAvroSerde<>();
        Serde<LightringLeds> lightringLedsSerde = new SpecificAvroSerde<>();

        final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url", schemaRegistryUrl);
        hazardDetectionVectorSerde.configure(serdeConfig, false);
        lightringLedsSerde.configure(serdeConfig, false);

        // create store
        StoreBuilder<KeyValueStore<String, String>> storeBuilder = Stores.keyValueStoreBuilder(
                Stores.inMemoryKeyValueStore(HazardProcessor.STORE_NAME),
                Serdes.String(),
                Serdes.String());

        // register store
        builder.addStateStore(storeBuilder);


        builder.stream(INPUT_TOPIC, Consumed.with(Serdes.String(), hazardDetectionVectorSerde))
                .mapValues(value -> value.getDetections().size() > 0 ? HazardProcessor.STATE_HAZARD : HazardProcessor.STATE_NO_HAZARD)
                //.peek((key, value) -> logger.info("before key: {}, value: {}", key, value))
                .to(INNER_TOPIC, Produced.with(Serdes.String(), Serdes.String()));


        builder.stream(INNER_TOPIC, Consumed.with(Serdes.String(), Serdes.String()))
                .groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(3)).advanceBy(Duration.ofMillis(250)))
                .reduce(
                        ((value1, value2) -> value1.equals(value2) ? value1 : HazardProcessor.STATE_NO_HAZARD)
                )
                .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded()))
                .toStream()
                .process(HazardProcessor::new, HazardProcessor.STORE_NAME)
                .peek((key, value) -> logger.info("out key: {}, value: {}", key, value))
                .map((key, value) -> {
                    List<LedColor> leds = new ArrayList<>();
                    for (int i = 0; i < 6; i++) {
                        if (value.equals(HazardProcessor.STATE_HAZARD)){
                            leds.add(new LedColor(255, 0, 0));
                        } else {
                            leds.add(new LedColor(0, 255, 0));
                        }
                    }
                    Header header = new Header(new Time((int) (System.currentTimeMillis() / 1000), 0), "0");
                    return new KeyValue<>(key, new LightringLeds(header, leds, true));
                })
                .peek((key, value) -> logger.info("final key: {}, value: {}", key, value))
                .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), lightringLedsSerde));

        return builder.build();
    }
}
