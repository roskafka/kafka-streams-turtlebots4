package de.hfu;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.ArrayList;
import java.util.List;

public class HazardProcessor implements Processor<String, HazardDetectionVector, String, String> {

    private ProcessorContext<String, String> context;
    private KeyValueStore<String, String> hazardState;

    static final String STORE_NAME = "hazardState";

    public static final String STATE_HAZARD = "HAZARD";
    public static final String STATE_NO_HAZARD = "NO_HAZARD";

    private long lastSent = 0;
    private long sendInterval = 1000;

    @Override
    public void init(ProcessorContext<String, String> context) {
        Processor.super.init(context);
        this.context = context;

        hazardState =  context.getStateStore(STORE_NAME);
    }

    @Override
    public void process(Record<String, HazardDetectionVector> record) {
        boolean hazardDetected = record.value().getDetections().size() > 0;
        String state = hazardDetected ? STATE_HAZARD : STATE_NO_HAZARD;
        context.forward(new Record<>(record.key(), state, System.currentTimeMillis()));
        if (record.timestamp() - sendInterval > lastSent) {
            lastSent = record.timestamp();
        }
    }

    private void setState(String robot, String state) {
        hazardState.put(robot, state);
    }
}
