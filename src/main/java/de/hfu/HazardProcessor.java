package de.hfu;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.ArrayList;
import java.util.List;

public class HazardProcessor implements Processor<String, HazardDetectionVector, String, LightringLeds> {

    private ProcessorContext context;
    private KeyValueStore<String, String> hazardState;

    static final String STORE_NAME = "hazardState";

    private static final String STATE_HAZARD = "HAZARD";
    private static final String STATE_NO_HAZARD = "NO_HAZARD";

    private int lastSent = 0;
    private static final int SEND_INTERVAL_SECONDS = 1;

    @Override
    public void init(ProcessorContext<String, LightringLeds> context) {
        Processor.super.init(context);
        this.context = context;

        hazardState =  context.getStateStore(STORE_NAME);
    }

    @Override
    public void process(Record<String, HazardDetectionVector> record) {
        boolean hazardDetected = record.value().getDetections().size() > 0;
        String currentState = hazardState.get(record.key());
        if (currentState == null){
            currentState = "";
        }
        boolean isCurrentStateHazard = currentState.equals(STATE_HAZARD);
        int currentSeconds = (int) (System.currentTimeMillis() / 1000);
        if (hazardDetected != isCurrentStateHazard || currentSeconds - SEND_INTERVAL_SECONDS > lastSent) {
            String state = hazardDetected ? STATE_HAZARD : STATE_NO_HAZARD;
            setState(record.key(), state);
            List<LedColor> leds = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                if (state.equals(STATE_HAZARD)){
                    leds.add(new LedColor(255, 0, 0));
                } else {
                    leds.add(new LedColor(0, 255, 0));
                }
            }
            lastSent = currentSeconds;
            Header header = new Header(new Time(currentSeconds, 0), "0");
            LightringLeds lightringLeds = new LightringLeds(header, leds, true);
            Record<String, LightringLeds> ledRecord = new Record<>(record.key(),  lightringLeds,  System.currentTimeMillis());
            context.forward(ledRecord);
        }
    }

    private void setState(String robot, String state) {
        hazardState.put(robot, state);
    }
}
