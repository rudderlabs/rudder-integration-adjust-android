package com.rudderstack.android.integration.adjust;

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
import com.adjust.sdk.OnDeferredDeeplinkResponseListener;
import com.adjust.sdk.OnEventTrackingFailedListener;
import com.adjust.sdk.OnEventTrackingSucceededListener;
import com.adjust.sdk.OnSessionTrackingFailedListener;
import com.adjust.sdk.OnSessionTrackingSucceededListener;
import com.rudderstack.android.sdk.core.MessageType;
import com.rudderstack.android.sdk.core.RudderClient;
import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderIntegration;
import com.rudderstack.android.sdk.core.RudderLogger;
import com.rudderstack.android.sdk.core.RudderMessage;


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

    private AdjustIntegrationFactory(Object config, final RudderClient client, RudderConfig rudderConfig) {
        this.adjust = Adjust.getDefaultInstance();
        String apiToken = "";
        Map<String, Object> destinationConfig = (Map<String, Object>) config;
        if (destinationConfig != null && destinationConfig.containsKey("appToken")) {
            apiToken = destinationConfig.get("appToken").toString();
        }
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
                RudderClient.getApplication(),
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
            public void onEventTrackingSucceeded(AdjustEventSuccess adjustEventSuccess) {
                Log.d("AdjustFactory", "Event success callback called!");
                Log.d("AdjustFactory", "Event success data: " + adjustEventSuccess.toString());
            }
        });
        adjustConfig.setOnEventTrackingFailedListener(new OnEventTrackingFailedListener() {
            @Override
            public void onEventTrackingFailed(AdjustEventFailure adjustEventFailure) {
                Log.d("AdjustFactory", "Event failure callback called!");
                Log.d("AdjustFactory", "Event failure data: " + adjustEventFailure.toString());
            }
        });
        adjustConfig.setOnSessionTrackingSucceededListener(new OnSessionTrackingSucceededListener() {
            @Override
            public void onSessionTrackingSucceeded(AdjustSessionSuccess adjustSessionSuccess) {
                Log.d("AdjustFactory", "Session success callback called!");
                Log.d("AdjustFactory", "Session success data: " + adjustSessionSuccess.toString());
            }
        });
        adjustConfig.setOnSessionTrackingFailedListener(new OnSessionTrackingFailedListener() {
            @Override
            public void onSessionTrackingFailed(AdjustSessionFailure adjustSessionFailure) {
                Log.d("AdjustFactory", "Session failure callback called!");
                Log.d("AdjustFactory", "Session failure data: " + adjustSessionFailure.toString());
            }
        });
        adjustConfig.setOnDeferredDeeplinkResponseListener(new OnDeferredDeeplinkResponseListener() {
            @Override
            public boolean launchReceivedDeeplink(Uri deeplink) {
                Log.d("AdjustFactory", "Deferred deep link callback called!");
                Log.d("AdjustFactory", "Deep link URL: " + deeplink);
                return true;
            }
        });
        adjustConfig.enableSendingInBackground();
        Adjust.initSdk(adjustConfig);
        if (RudderClient.getApplication() != null) {
            RudderClient.getApplication().registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
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
                    if (eventToken == null || eventToken.isEmpty()) {
                        break;
                    }

                    this.setSessionParams(element);
                    AdjustEvent event = new AdjustEvent(eventToken);
                    Map<String, Object> eventProperties = element.getProperties();
                    if (eventProperties != null) {
                        for (String key : eventProperties.keySet()) {
                            event.addCallbackParameter(key, String.valueOf(eventProperties.get(key)));
                        }
                        if (eventProperties.containsKey("revenue") && eventProperties.containsKey("currency")) {
                            event.setRevenue(
                                    Double.parseDouble(String.valueOf(eventProperties.get("revenue"))),
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
                    this.setSessionParams(element);
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

    private void setSessionParams(RudderMessage element) {
        Adjust.addGlobalPartnerParameter("anonymousId", element.getAnonymousId());
        if (!TextUtils.isEmpty(element.getUserId())) {
            Adjust.addGlobalPartnerParameter("userId", element.getUserId());
        }
    }

    @Override
    public void reset() {
        Adjust.removeGlobalPartnerParameters();
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
