package com.rudderstack.android.integration.adjust;

import com.adjust.sdk.AdjustAttribution;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.LogLevel;
import com.rudderstack.android.sdk.core.RudderClient;
import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderLogger;
import com.rudderstack.android.sdk.core.RudderProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for Adjust integration helper methods.
 */
public class Utils {

    /**
     * Sends an "Install Attributed" event to RudderStack when attribution data is received from Adjust.
     *
     * @param rudderClient      The RudderClient instance to send the event through
     * @param attribution The attribution data from Adjust (can be null for initial event)
     */
    public static void sendInstallAttributedEvent(RudderClient rudderClient, AdjustAttribution attribution) {
        // Create properties following the reference implementation structure
        RudderProperty properties = new RudderProperty();
        Map<String, Object> propertiesMap = new HashMap();
        propertiesMap.put("provider", "Adjust");

        // Add attribution properties with null checks
        if (attribution.trackerToken != null) {
            propertiesMap.put("trackerToken", attribution.trackerToken);
        }
        if (attribution.trackerName != null) {
            propertiesMap.put("trackerName", attribution.trackerName);
        }

        // Create campaign object with attribution data
        Map<String, Object> campaign = new HashMap<>();
        if (attribution.network != null) {
            campaign.put("source", attribution.network);
        }
        if (attribution.campaign != null) {
            campaign.put("name", attribution.campaign);
        }
        if (attribution.clickLabel != null) {
            campaign.put("content", attribution.clickLabel);
        }
        if (attribution.creative != null) {
            campaign.put("adCreative", attribution.creative);
        }
        if (attribution.adgroup != null) {
            campaign.put("adGroup", attribution.adgroup);
        }
        propertiesMap.put("campaign", campaign);

        properties.putValue(propertiesMap);

        // Send the Install Attributed event
        rudderClient.track("Install Attributed", properties);
        RudderLogger.logInfo("AdjustUtils: Install Attributed event sent successfully with properties: " + propertiesMap);
    }

    /**
     * Sets the appropriate log level for Adjust SDK based on RudderStack configuration.
     *
     * @param rudderConfig The RudderStack configuration containing log level
     * @param adjustConfig The Adjust configuration to apply the log level to
     */
    public static void setLogLevel(RudderConfig rudderConfig, AdjustConfig adjustConfig) {
        if (rudderConfig.getLogLevel() == RudderLogger.RudderLogLevel.VERBOSE) {
            adjustConfig.setLogLevel(LogLevel.VERBOSE);
        } else if (rudderConfig.getLogLevel() == RudderLogger.RudderLogLevel.DEBUG) {
            adjustConfig.setLogLevel(LogLevel.DEBUG);
        } else if (rudderConfig.getLogLevel() == RudderLogger.RudderLogLevel.INFO) {
            adjustConfig.setLogLevel(LogLevel.INFO);
        } else if (rudderConfig.getLogLevel() == RudderLogger.RudderLogLevel.WARN) {
            adjustConfig.setLogLevel(LogLevel.WARN);
        } else if (rudderConfig.getLogLevel() == RudderLogger.RudderLogLevel.ERROR) {
            adjustConfig.setLogLevel(LogLevel.ERROR);
        } else {
            adjustConfig.setLogLevel(LogLevel.SUPPRESS);
        }
    }
}
