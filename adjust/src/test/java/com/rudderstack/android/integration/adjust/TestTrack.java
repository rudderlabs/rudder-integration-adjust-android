package com.rudderstack.android.integration.adjust;

import androidx.annotation.Nullable;

import com.adjust.sdk.AdjustEvent;

import java.util.List;
import java.util.Objects;

public class TestTrack {
    public TestTrack(@Nullable List<String> identify, List<AdjustEvent> events) {
        this.identify = identify;
        this.events = events;
    }

    @Nullable
    List<String> identify;
    List<AdjustEvent> events;


    @Override
    public boolean equals(Object o) {
        return this.checkForIdentify(o) && this.checkForTrack(o);
    }

    private boolean checkForIdentify(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestTrack)) return false;
        TestTrack that = (TestTrack) o;
        if (!Objects.equals(identify, that.identify)) {
            throw new AssertionError("The 'identify' lists are not equal.");
        }
        return true;
    }

    private boolean checkForTrack(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestTrack)) return false;
        TestTrack that = (TestTrack) o;
        AdjustEvent actual = events.get(0);
        AdjustEvent expected = that.events.get(0);
        for (String params: actual.callbackParameters.keySet()) {
            if (!Objects.equals(actual.callbackParameters.get(params), expected.callbackParameters.get(params))) {
                throw new AssertionError("The 'track' callbackParameters lists are not equal.");
            }
        }
        if (!Objects.equals(actual.eventToken, expected.eventToken)) {
            throw new AssertionError("The 'track' eventToken are not equal.");
        }
        if (!Objects.equals(actual.currency, expected.currency)) {
            throw new AssertionError("The 'track' currency are not equal.");
        }
        if (!Objects.equals(actual.revenue, expected.revenue)) {
            throw new AssertionError("The 'track' revenue are not equal.");
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(identify, events);
    }
}
