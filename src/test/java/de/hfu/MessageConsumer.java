package de.hfu;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class MessageConsumer<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);

    private final Duration duration;
    private final String topic;
    private final MessageMatcher<V> messageMatcher;
    private final Integer minMessages;
    private final Integer maxMessages;

    private final KafkaConsumer<K, V> consumer;

    private Error error;

    private Thread thread;


    private MessageConsumer(Duration duration, String topic, MessageMatcher<V> messageMatcher, Integer minMessages, Integer maxMessages) {
        this.duration = duration;
        this.topic = topic;
        this.messageMatcher = messageMatcher;
        this.minMessages = minMessages;
        this.maxMessages = maxMessages;

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");  // replace with your Kafka server details
        props.put("group.id", "test");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "io.confluent.kafka.serializers.KafkaAvroDeserializer");
        props.put("schema.registry.url", "http://localhost:8081");

        consumer = new KafkaConsumer<>(props);
    }

    public static MessageBuilder builder() {
        return new MessageBuilder();
    }

    public Thread start() {
        thread = new Thread(this::run);
        thread.start();
        return thread;
    }

    public void join()  {
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (error != null) {
            throw error;
        }
    }

    private void run() {
        try {
            consumer.subscribe(Collections.singletonList(topic));

            long end = System.currentTimeMillis() + duration.toMillis();
            AtomicInteger messageCount = new AtomicInteger();

            while (System.currentTimeMillis() < end && (maxMessages == null || messageCount.get() <= maxMessages)) {
                ConsumerRecords<K, V> records = consumer.poll(Duration.ofMillis(100));

                records.forEach(record -> {
                    if (messageMatcher.test(record.value())) {
                        messageCount.getAndIncrement();

                        if (minMessages != null && messageCount.get() < minMessages) {
                            error = new AssertionError("Expected at least " + minMessages + " messages but received " + messageCount);
                            throw error;
                        }
                    } else {
                        error = new AssertionError("Message did not match predicate: " + record.value());
                        throw error;
                    }
                });
            }

            if (maxMessages != null && messageCount.get() > maxMessages) {
                error = new AssertionError("Expected at most " + maxMessages + " messages but received " + messageCount);
                throw error;
            }
            if (minMessages != null && messageCount.get() < minMessages) {
                error = new AssertionError("Expected at least " + minMessages + " messages but received " + messageCount);
                throw error;
            }
        } finally {
            consumer.close();
        }
    }

    public static class MessageBuilder {
        private Duration duration;
        private String topic;
        private MessageMatcher messageMatcher;
        private Integer minMessages;
        private Integer maxMessages;

        public ConsumeBuilder consume() {
            return new ConsumeBuilder(this);
        }

        public <K, V> MessageConsumer<K, V> build() {
            // Here you should handle what happens when the consumer is built.
            // You may want to start consuming messages, for example.
            return new MessageConsumer<>(duration, topic, messageMatcher, minMessages, maxMessages);
        }
    }



    public static class ConsumeBuilder {
        private final MessageBuilder builder;

        public ConsumeBuilder(MessageBuilder builder) {
            this.builder = builder;
        }

        public ConsumeBuilder until(Duration duration) {
            builder.duration = duration;
            return this;
        }

        public ConsumeBuilder fromTopic(String topic) {
            builder.topic = topic;
            return this;
        }

        public ConsumeBuilder expectAtLeast(int n, MessageMatcher messageMatcher) {
            builder.minMessages = n;
            builder.messageMatcher = messageMatcher;
            return this;
        }

        public ConsumeBuilder expectAtMost(int n, MessageMatcher messageMatcher) {
            builder.maxMessages = n;
            builder.messageMatcher = messageMatcher;
            return this;
        }

        public MessageBuilder and() {
            return builder;
        }
    }

    public static class MessageMatcher<V> {

        private final Predicate<V> messageMatcher;

        public MessageMatcher(Predicate<V> messageMatcher) {
            this.messageMatcher = messageMatcher;
        }

        public boolean test(V v) {
        	return messageMatcher.test(v);
        }
    }
}
