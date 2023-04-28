package com.rudderstack.android.integration.adjust;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.Objects;

public class TestIdentify {
    public TestIdentify(@Nullable List<String> identify) {
        this.identify = identify;
    }

    @Nullable
    List<String> identify;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestIdentify)) return false;
        TestIdentify that = (TestIdentify) o;
        if (!Objects.equals(identify, that.identify)) {
            throw new AssertionError("The 'identify' lists are not equal.");
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(identify);
    }
}