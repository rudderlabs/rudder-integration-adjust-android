package com.rudderstack.android.integration.adjust;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rudderstack.android.sdk.core.RudderLogger;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

public class Utils {
    public static Map<String, String> getMappedRudderEvents(JsonArray mappedERudderEvents) {
        if(isEmpty(mappedERudderEvents)) {
            return null;
        }
        Map<String, String> mappedEvents = new HashMap<>();
        for (int i = 0; i < mappedERudderEvents.size(); i++) {
            JsonObject eventObject = (JsonObject) mappedERudderEvents.get(i);
            String eventName = eventObject.get("from").getAsString();
            String eventValue = eventObject.get("to").getAsString();
            mappedEvents.put(eventName, eventValue);
        }
        return mappedEvents;
    }

    public static boolean isEmpty(Object value) {
        if(value == null){
            return true;
        }

        if (value instanceof JsonArray) {
            return (((JsonArray) value).size() == 0);
        }

        if (value instanceof JSONArray) {
            return (((JSONArray) value).length() == 0);
        }

        if (value instanceof Map) {
            return (((Map<?, ?>) value).size() == 0);
        }

        if (value instanceof String) {
            return (((String) value).trim().isEmpty());
        }

        return false;
    }

    public static String getString(Object value) {
        if (value instanceof String) {
            return (String) value;
        } else if (value != null) {
            return String.valueOf(value);
        }
        return null;
    }

    public static double getDouble(Object value, double defaultValue) {
        if (value instanceof Double) {
            return (double) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException ignored) {
                RudderLogger.logDebug("Unable to convert the value: " + value +
                        " to Double, using the defaultValue: " + defaultValue);
            }
        }
        return defaultValue;
    }
}
