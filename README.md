# What is RudderStack?

[RudderStack](https://rudderstack.com/) is a **customer data pipeline** tool for collecting, routing and processing data from your websites, apps, cloud tools, and data warehouse.

More information on RudderStack can be found [here](https://github.com/rudderlabs/rudder-server).

## Getting Started with Adjust Integration of Android SDK

1. Add [Adjust](https://www.adjust.com) as a destination in the [RudderStack dashboard](https://app.rudderstack.com/) and define ```apiToken``` and ```eventMapping```

2. Add these lines to your ```app/build.gradle```:

```
repositories {
  maven { url  "https://dl.bintray.com/rudderstack/rudderstack" }
}
```

3. Add the dependency under ```dependencies```
```
implementation 'com.rudderstack.android.sdk:core:1.0.1'
implementation 'com.rudderstack.android.integration:adjust:0.1.0'
implementation 'com.adjust.sdk:adjust-android:4.19.1'
```

## Initialize ```RudderClient```

```
val rudderClient: RudderClient = RudderClient.getInstance(
    this,
    <WRITE_KEY>,
    RudderConfig.Builder()
        .withDataPlaneUrl(<DATA_PLANE_URL>)
        .withLogLevel(RudderLogger.RudderLogLevel.DEBUG)
        .withFactory(AdjustIntegrationFactory.FACTORY)
        .build()
)
```

## Send Events

Follow the steps from [RudderStack Android SDK](https://github.com/rudderlabs/rudder-sdk-android).

## Contact Us
If you come across any issues while configuring or using this SDK, please feel free to start a conversation on our [Slack](https://resources.rudderstack.com/join-rudderstack-slack) channel. We will be happy to help you.
