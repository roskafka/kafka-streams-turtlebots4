package de.hfu;

import org.apache.kafka.streams.KeyValue;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class TestHazardDetection {

    @Test
    void testNoHazardDetected() throws InterruptedException {
        var publisher = MessagePublisher.builder()
                .interval()
                .every(Duration.ofMillis(100))
                .until(Duration.ofSeconds(10))
                .send()
                .message(ts -> new KeyValue<>("1", new HazardDetectionVector(
                                new Header(new Time((int) (System.currentTimeMillis() / 1000), 0), "0"),
                                new ArrayList<>())
                        )
                )
                .toTopic("roskafka-hazards")
                .build();

        var consumer = MessageConsumer.builder()
                .consume()
                .until(Duration.ofSeconds(10))
                .fromTopic("kafkaros-leds")
//                    .expectAtLeast(1, Duration.ofSeconds(10), msg -> true)
                .expectAtMost(0, new MessageConsumer.MessageMatcher<>(msg -> true))
                .and()
                .build();

        publisher.start();
        consumer.start();

        publisher.join();
        consumer.join();
    }

    @Test
    void testLedsTurnRedAfterHazardDetectedFor5Seconds() {
        var publisher = MessagePublisher.builder()
                .interval()
                .every(Duration.ofMillis(100))
                .until(Duration.ofSeconds(10))
                .send()
                .message(ts -> new KeyValue<>("1", new HazardDetectionVector(
                                new Header(new Time((int) (System.currentTimeMillis() / 1000), 0), "0"),
                                // List of at least 3 detections
                                List.of(
                                        new HazardDetection(new Header(new Time((int) (System.currentTimeMillis() / 1000), 0), "wheel_left"), 1),
                                        new HazardDetection(new Header(new Time((int) (System.currentTimeMillis() / 1000), 0), "wheel_right"), 1),
                                        new HazardDetection(new Header(new Time((int) (System.currentTimeMillis() / 1000), 0), "wheel_rear"), 1)
                                )
                        )
                        )
                )
                .toTopic("roskafka-hazards")
                .build();

        var consumer = MessageConsumer.builder()
                .consume()
                .until(Duration.ofSeconds(10))
                .fromTopic("kafkaros-leds")
                // expect red leds
                .expectAtLeast(1, new MessageConsumer.MessageMatcher<LightringLeds>(msg -> {
                    for (LedColor ledColor : msg.getLeds()) {
                        if (ledColor.getRed() != 255 || ledColor.getGreen() != 0 || ledColor.getBlue() != 0) {
                            return false;
                        }
                    }
                    return true;
                }))
                .and()
                .build();

        publisher.start();
        consumer.start();

        publisher.join();
        consumer.join();
    }

}
