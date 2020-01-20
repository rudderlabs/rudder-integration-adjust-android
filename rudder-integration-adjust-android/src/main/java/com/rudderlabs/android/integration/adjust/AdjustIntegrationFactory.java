package com.rudderlabs.android.integration.adjust;

import android.app.Activity;
import android.app.Application;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustAttribution;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustEvent;
import com.adjust.sdk.AdjustEventFailure;
import com.adjust.sdk.AdjustEventSuccess;
import com.adjust.sdk.AdjustInstance;
import com.adjust.sdk.AdjustSessionFailure;
import com.adjust.sdk.AdjustSessionSuccess;
import com.adjust.sdk.LogLevel;
import com.adjust.sdk.OnAttributionChangedListener;
import com.adjust.sdk.OnDeeplinkResponseListener;
import com.adjust.sdk.OnEventTrackingFailedListener;
import com.adjust.sdk.OnEventTrackingSucceededListener;
import com.adjust.sdk.OnSessionTrackingFailedListener;
import com.adjust.sdk.OnSessionTrackingSucceededListener;
import com.rudderlabs.android.sdk.core.MessageType;
import com.rudderlabs.android.sdk.core.RudderClient;
import com.rudderlabs.android.sdk.core.RudderConfig;
import com.rudderlabs.android.sdk.core.RudderIntegration;
import com.rudderlabs.android.sdk.core.RudderLogger;
import com.rudderlabs.android.sdk.core.RudderMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdjustIntegrationFactory extends RudderIntegration<AdjustInstance> {
    private static final String ADJUST_KEY = "Adjust";
    private final AdjustInstance adjust;
    private Map<String, String> eventMap = new HashMap<>();

    public static Factory FACTORY = new Factory() {
        @Override
        public RudderIntegration<?> create(Object settings, RudderClient client, RudderConfig rudderConfig) {
            return new AdjustIntegrationFactory(settings, client, rudderConfig);
        }

        @Override
        public String key() {
            return ADJUST_KEY;
        }
    };

    private AdjustIntegrationFactory(Object config, RudderClient client, RudderConfig rudderConfig) {
        this.adjust = Adjust.getDefaultInstance();
        String apiToken = "";
        Map<String, Object> destinationConfig = (Map<String, Object>) config;
        if (destinationConfig != null && destinationConfig.containsKey("appToken"))
            apiToken = (String) destinationConfig.get("appToken");
        if (destinationConfig != null && destinationConfig.containsKey("customMappings")) {
            List<Object> eventList = (List<Object>) destinationConfig.get("customMappings");
            if (eventList != null && !eventList.isEmpty()) {
                for (Object item : eventList) {
                    Map<String, String> keyMap = (Map<String, String>) item;
                    if (keyMap != null && keyMap.containsKey("from") && keyMap.containsKey("to")) {
                        eventMap.put(keyMap.get("from"), keyMap.get("to"));
                    }
                }
            }
        }

        AdjustConfig adjustConfig = new AdjustConfig(
                client.getApplication(),
                apiToken,
                rudderConfig.getLogLevel() >= RudderLogger.RudderLogLevel.DEBUG ? AdjustConfig.ENVIRONMENT_SANDBOX : AdjustConfig.ENVIRONMENT_PRODUCTION
        );
        adjustConfig.setLogLevel(rudderConfig.getLogLevel() >= RudderLogger.RudderLogLevel.DEBUG ? LogLevel.VERBOSE : LogLevel.ERROR);

        adjustConfig.setOnAttributionChangedListener(new OnAttributionChangedListener() {
            @Override
            public void onAttributionChanged(AdjustAttribution attribution) {
                Log.d("AdjustFactory", "Attribution callback called!");
                Log.d("AdjustFactory", "Attribution: " + attribution.toString());
            }
        });
        adjustConfig.setOnEventTrackingSucceededListener(new OnEventTrackingSucceededListener() {
            @Override
            public void onFinishedEventTrackingSucceeded(AdjustEventSuccess eventSuccessResponseData) {
                Log.d("AdjustFactory", "Event success callback called!");
                Log.d("AdjustFactory", "Event success data: " + eventSuccessResponseData.toString());
            }
        });
        adjustConfig.setOnEventTrackingFailedListener(new OnEventTrackingFailedListener() {
            @Override
            public void onFinishedEventTrackingFailed(AdjustEventFailure eventFailureResponseData) {
                Log.d("AdjustFactory", "Event failure callback called!");
                Log.d("AdjustFactory", "Event failure data: " + eventFailureResponseData.toString());
            }
        });
        adjustConfig.setOnSessionTrackingSucceededListener(new OnSessionTrackingSucceededListener() {
            @Override
            public void onFinishedSessionTrackingSucceeded(AdjustSessionSuccess sessionSuccessResponseData) {
                Log.d("AdjustFactory", "Session success callback called!");
                Log.d("AdjustFactory", "Session success data: " + sessionSuccessResponseData.toString());
            }
        });
        adjustConfig.setOnSessionTrackingFailedListener(new OnSessionTrackingFailedListener() {
            @Override
            public void onFinishedSessionTrackingFailed(AdjustSessionFailure sessionFailureResponseData) {
                Log.d("AdjustFactory", "Session failure callback called!");
                Log.d("AdjustFactory", "Session failure data: " + sessionFailureResponseData.toString());
            }
        });
        adjustConfig.setOnDeeplinkResponseListener(new OnDeeplinkResponseListener() {
            @Override
            public boolean launchReceivedDeeplink(Uri deeplink) {
                Log.d("AdjustFactory", "Deferred deep link callback called!");
                Log.d("AdjustFactory", "Deep link URL: " + deeplink);
                return true;
            }
        });
        adjustConfig.setSendInBackground(true);
        this.adjust.onCreate(adjustConfig);
        if (client.getApplication() != null) {
            client.getApplication().registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(Activity activity, Bundle bundle) {

                }

                @Override
                public void onActivityStarted(Activity activity) {

                }

                @Override
                public void onActivityResumed(Activity activity) {
                    Adjust.onResume();
                }

                @Override
                public void onActivityPaused(Activity activity) {
                    Adjust.onPause();
                }

                @Override
                public void onActivityStopped(Activity activity) {

                }

                @Override
                public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

                }

                @Override
                public void onActivityDestroyed(Activity activity) {

                }
            });
        }
    }

    private void processRudderEvent(RudderMessage element) {
        if (element != null && element.getType() != null) {
            switch (element.getType()) {
                case MessageType.TRACK:
                    // check pre-defined event map and find out the token for event
                    String eventToken = null;
                    if (eventMap.containsKey(element.getEventName())) {
                        eventToken = eventMap.get(element.getEventName());
                    }
                    // if event is not tracked using Adjust (eventToken from Adjust is null)
                    if (eventToken == null) {
                        break;
                    }

                    AdjustEvent event = new AdjustEvent(eventToken);
                    Map<String, Object> eventProperties = element.getProperties();
                    if (eventProperties != null) {
                        for (String key : eventProperties.keySet()) {
                            event.addCallbackParameter(key, String.valueOf(eventProperties.get(key)));
                        }
                        if (eventProperties.containsKey("total") && eventProperties.containsKey("currency")) {
                            event.setRevenue(
                                    Double.parseDouble(String.valueOf(eventProperties.get("total"))),
                                    String.valueOf(eventProperties.get("currency"))
                            );
                        }
                    }
                    Map<String, Object> userProperties = element.getUserProperties();
                    if (userProperties != null) {
                        for (String key : userProperties.keySet()) {
                            event.addCallbackParameter(key, String.valueOf(userProperties.get(key)));
                        }
                    }
                    this.adjust.trackEvent(event);
                    break;
                case MessageType.IDENTIFY:

                    Adjust.addSessionPartnerParameter("anonymous_id", element.getAnonymousId());
                    if (!TextUtils.isEmpty(element.getUserId())) {
                        Adjust.addSessionPartnerParameter("user_id", element.getUserId());
                    }
                    break;
                case MessageType.SCREEN:
                    RudderLogger.logWarn("AdjustIntegrationFactory: MessageType is not supported");
                    break;
                default:
                    RudderLogger.logWarn("AdjustIntegrationFactory: MessageType is not specified");
                    break;
            }
        }
    }

    @Override
    public void reset() {
        this.adjust.resetSessionCallbackParameters();
        this.adjust.resetSessionPartnerParameters();
    }

    @Override
    public void dump(RudderMessage element) {
        processRudderEvent(element);
    }

    @Override
    public AdjustInstance getUnderlyingInstance() {
        return adjust;
    }
}
