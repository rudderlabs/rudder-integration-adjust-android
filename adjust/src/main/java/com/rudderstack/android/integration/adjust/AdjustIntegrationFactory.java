package com.rudderstack.android.integration.adjust;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustEvent;
import com.adjust.sdk.AdjustInstance;
import com.adjust.sdk.LogLevel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.rudderstack.android.sdk.core.MessageType;
import com.rudderstack.android.sdk.core.RudderClient;
import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderIntegration;
import com.rudderstack.android.sdk.core.RudderLogger;
import com.rudderstack.android.sdk.core.RudderMessage;
import androidx.annotation.*;

import java.util.Map;

public class AdjustIntegrationFactory extends RudderIntegration<AdjustInstance> {
    private static final String ADJUST_KEY = "Adjust";
    private final AdjustInstance adjust;
    private final AdjustDestinationConfig destinationConfig;

    public static Factory FACTORY = new Factory() {
        @Override
        public RudderIntegration<?> create(Object settings, RudderClient client, RudderConfig rudderConfig) {
            return new AdjustIntegrationFactory(settings, rudderConfig);
        }

        @Override
        public String key() {
            return ADJUST_KEY;
        }
    };

    @VisibleForTesting
    AdjustIntegrationFactory(AdjustInstance adjust, Object config) {
        this.adjust = adjust;
        this.destinationConfig = createAdjustConfig(config);
    }

    private AdjustIntegrationFactory(Object config, RudderConfig rudderConfig) {
        this(Adjust.getDefaultInstance(), config);

        AdjustConfig adjustConfig = new AdjustConfig(
                RudderClient.getApplication(),
                destinationConfig.getApiToken(),
                rudderConfig.getLogLevel() >= RudderLogger.RudderLogLevel.DEBUG ? AdjustConfig.ENVIRONMENT_SANDBOX : AdjustConfig.ENVIRONMENT_PRODUCTION
        );
        adjustConfig.setLogLevel(rudderConfig.getLogLevel() >= RudderLogger.RudderLogLevel.DEBUG ? LogLevel.VERBOSE : LogLevel.ERROR);
        if (destinationConfig.getDelay() > 0) {
            adjustConfig.setDelayStart(destinationConfig.getDelay());
        }

        adjustConfig.setOnAttributionChangedListener(attribution -> {
            Log.d("AdjustFactory", "Attribution callback called!");
            Log.d("AdjustFactory", "Attribution: " + attribution.toString());
        });
        adjustConfig.setOnEventTrackingSucceededListener(eventSuccessResponseData -> {
            Log.d("AdjustFactory", "Event success callback called!");
            Log.d("AdjustFactory", "Event success data: " + eventSuccessResponseData.toString());
        });
        adjustConfig.setOnEventTrackingFailedListener(eventFailureResponseData -> {
            Log.d("AdjustFactory", "Event failure callback called!");
            Log.d("AdjustFactory", "Event failure data: " + eventFailureResponseData.toString());
        });
        adjustConfig.setOnSessionTrackingSucceededListener(sessionSuccessResponseData -> {
            Log.d("AdjustFactory", "Session success callback called!");
            Log.d("AdjustFactory", "Session success data: " + sessionSuccessResponseData.toString());
        });
        adjustConfig.setOnSessionTrackingFailedListener(sessionFailureResponseData -> {
            Log.d("AdjustFactory", "Session failure callback called!");
            Log.d("AdjustFactory", "Session failure data: " + sessionFailureResponseData.toString());
        });
        adjustConfig.setOnDeeplinkResponseListener(deeplink -> {
            Log.d("AdjustFactory", "Deferred deep link callback called!");
            Log.d("AdjustFactory", "Deep link URL: " + deeplink);
            return true;
        });
        adjustConfig.setSendInBackground(true);
        this.adjust.onCreate(adjustConfig);
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

    private AdjustDestinationConfig createAdjustConfig(Object config) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        JsonDeserializer<AdjustDestinationConfig> deserializer =
                (json, typeOfT, context) -> {
                    JsonObject jsonObject = json.getAsJsonObject();
                    String appToken = Utils.getString(jsonObject.get("appToken").getAsString());
                    JsonArray customMappings =
                            (JsonArray) (jsonObject.get("customMappings"));
                    Map<String, String> eventMap = Utils.getMappedRudderEvents(customMappings);
                    double delay = jsonObject.get("delay").getAsDouble();
                    return new AdjustDestinationConfig(appToken, eventMap, delay);
                };
        gsonBuilder.registerTypeAdapter(AdjustDestinationConfig.class, deserializer);
        Gson customGson = gsonBuilder.create();
        return customGson.fromJson(customGson.toJson(config), AdjustDestinationConfig.class);
    }


    private void processRudderEvent(RudderMessage element) {
        if (element != null && element.getType() != null) {
            switch (element.getType()) {
                case MessageType.TRACK:
                    String eventToken = destinationConfig.getCorrespondingTokenForMappedEvent(element.getEventName());
                    // if event is not tracked using Adjust (eventToken from Adjust is null)
                    if (eventToken == null || eventToken.isEmpty()) {
                        RudderLogger.logDebug("Dropping the track event, since corresponding event token is not present.");
                        return;
                    }
                    this.setSessionParams(element);
                    AdjustEvent event = new AdjustEvent(eventToken);
                    Map<String, Object> eventProperties = element.getProperties();
                    if (eventProperties != null) {
                        for (String key : eventProperties.keySet()) {
                            event.addCallbackParameter(key, Utils.getString(eventProperties.get(key)));
                        }
                        if (eventProperties.containsKey("revenue") && eventProperties.containsKey("currency")) {
                            event.setRevenue(
                                    Utils.getDouble(eventProperties.get("revenue"), 0.0),
                                    Utils.getString(eventProperties.get("currency"))
                            );
                        }
                    }
                    Map<String, Object> userProperties = element.getUserProperties();
                    if (userProperties != null) {
                        for (String key : userProperties.keySet()) {
                            event.addCallbackParameter(key, Utils.getString(userProperties.get(key)));
                        }
                    }
                    this.adjust.trackEvent(event);
                    RudderLogger.logVerbose("AdjustIntegrationFactory: Track event is called, with eventToken: " + event.eventToken + " and callbackProperties: " + event.callbackParameters);
                    break;
                case MessageType.IDENTIFY:
                    this.setSessionParams(element);
                    break;
                default:
                    RudderLogger.logWarn("AdjustIntegrationFactory: MessageType is not supported");
                    break;
            }
        }
    }

    private void setSessionParams(RudderMessage element) {
        Adjust.addSessionPartnerParameter("anonymousId", element.getAnonymousId());
        if (!TextUtils.isEmpty(element.getUserId())) {
            Adjust.addSessionPartnerParameter("userId", element.getUserId());
        }
    }

    @Override
    public void reset() {
        this.adjust.resetSessionPartnerParameters();
        RudderLogger.logVerbose("AdjustIntegrationFactory: adjust.resetSessionPartnerParameters() API is called");
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
