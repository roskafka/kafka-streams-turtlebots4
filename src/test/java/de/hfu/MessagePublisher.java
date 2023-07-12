package de.hfu;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.streams.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Properties;
import java.util.function.Function;

public class MessagePublisher<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(MessagePublisher.class);
    private Duration interval;
    private final Function<Long, KeyValue<K, V>> messageFn;
    private String topic;

    private KafkaProducer<K, V> producer;

    private Thread thread;

    private MessagePublisher(Duration interval, Function<Long, KeyValue<K, V>> messageFn, String topic) {
        this.interval = interval;
        this.messageFn = messageFn;
        this.topic = topic;

        // Kafka Producer configuration settings
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092"); // replace with your Kafka server details
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");
        props.put("schema.registry.url", "http://localhost:8081");

        producer = new KafkaProducer<>(props);
    }

    public static MessageBuilder builder() {
        return new MessageBuilder();
    }

    public Thread start() {
        thread = new Thread(this::run);
        thread.start();
        return thread;
    }

    public void run() {
        long end = System.currentTimeMillis() + interval.toMillis();

        while (System.currentTimeMillis() < end) {
            KeyValue<K, V> message = messageFn.apply(System.currentTimeMillis());
            producer.send(new ProducerRecord<>(topic, message.key, message.value));

            try {
                Thread.sleep(interval.toMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
                // handle interruption
            }
        }

        producer.close();
        logger.info("Producer closed");
    }

    public void join() {
        // wait until the thread has finished
        try {
            thread.join();
        } catch (InterruptedException e) {
            // handle interruption
        }
        logger.info("Producer joined");
    }


    public static class MessageBuilder {
        private Duration interval;
        private Function<Long, KeyValue> messageFn;
        private String topic;

        public IntervalBuilder interval() {
            return new IntervalBuilder(this);
        }

        public SendBuilder send() {
            return new SendBuilder(this);
        }

        public MessagePublisher build() {
            // Here you should handle what happens when the message is built.
            // You may want to schedule a task to send messages, for example.
            return new MessagePublisher(interval, messageFn, topic);
        }
    }

    public static class IntervalBuilder {
        private MessageBuilder builder;

        public IntervalBuilder(MessageBuilder builder) {
            this.builder = builder;
        }

        public IntervalBuilder every(Duration duration) {
            builder.interval = duration;
            return this;
        }

        public MessageBuilder until(Duration duration) {
            // Here you might want to handle the 'until' logic.
            // This is currently just setting the interval to the provided duration.
            builder.interval = duration;
            return builder;
        }
    }

    public static class SendBuilder {
        private MessageBuilder builder;

        public SendBuilder(MessageBuilder builder) {
            this.builder = builder;
        }

        public SendBuilder message(Function<Long, KeyValue> messageFn) {
            builder.messageFn = messageFn;
            return this;
        }

        public MessageBuilder toTopic(String topic) {
            builder.topic = topic;
            return builder;
        }
    }
}
