package de.hfu;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class Ex {

    private static final String INPUT_TOPIC = "in_1";
    private static final String OUTPUT_TOPIC = "out_1";

    private static final Logger logger = LoggerFactory.getLogger(Ex.class);

    @Produces
    public Topology createPositionsStream() {

        StreamsBuilder builder = new StreamsBuilder();

        builder.stream(INPUT_TOPIC, Consumed.with(Serdes.String(), Serdes.String()))
                .peek((key, value) -> logger.info("key: {}, value: {}", key, value))
                .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));

        return builder.build();
    }
}
