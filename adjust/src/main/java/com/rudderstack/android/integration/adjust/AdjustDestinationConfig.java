package com.rudderstack.android.integration.adjust;

import java.util.Map;

public class AdjustDestinationConfig {
    private String apiToken;
    private Map<String, String> eventMap;
    private double delay;

    AdjustDestinationConfig (
            String apiToken,
            Map<String, String> eventMap,
            double delay) {
        this.setApiToken(apiToken);
        this.setEventMap(eventMap);
        this.setDelay(delay);
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setEventMap(Map<String, String> eventMap) {
        this.eventMap = eventMap;
    }

    public Map<String, String> getEventMap() {
        return eventMap;
    }

    public void setDelay(double delay) {
        this.delay = delay;
        if (this.delay < 0) {
            this.delay = 0;
        } else if (this.delay > 10) {
            this.delay = 10;
        }
    }

    public double getDelay() {
        return delay;
    }

    public String getCorrespondingTokenForMappedEvent(String eventName) {
        if (this.getEventMap().containsKey(eventName)) {
            return this.getEventMap().get(eventName);
        }
        return null;
    }
}
