package de.hfu;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.ArrayList;
import java.util.List;

public class HazardProcessor implements Processor<Windowed<String>, String, String, String> {

    private ProcessorContext<String, String> context;
    private KeyValueStore<String, String> hazardState;

    static final String STORE_NAME = "hazardState";

    public static final String STATE_HAZARD = "HAZARD";
    public static final String STATE_NO_HAZARD = "NO_HAZARD";

    @Override
    public void init(ProcessorContext<String, String> context) {
        Processor.super.init(context);
        this.context = context;

        hazardState =  context.getStateStore(STORE_NAME);
    }

    @Override
    public void process(Record<Windowed<String>, String> record) {
        String detectedState = record.value();
        String storedState = hazardState.get(record.key().key());
        if (!detectedState.equals(storedState)) {
            setState(record.key().key(), detectedState);
            Record<String, String> newRecord = new Record<>(record.key().key(),  detectedState,  System.currentTimeMillis());
            context.forward(newRecord);
        }
    }

    private void setState(String robot, String state) {
        hazardState.put(robot, state);
    }
}
