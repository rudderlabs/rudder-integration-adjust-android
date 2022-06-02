# What is RudderStack?

[RudderStack](https://rudderstack.com/) is a **customer data pipeline** tool for collecting, routing and processing data from your websites, apps, cloud tools, and data warehouse.

More information on RudderStack can be found [here](https://github.com/rudderlabs/rudder-server).

## Getting Started with Adjust Integration of Android SDK

1. Add [Adjust](https://www.adjust.com) as a destination in the [RudderStack dashboard](https://app.rudderstack.com/) and define ```apiToken``` and ```eventMapping```

2. Add the following permissions to your AndroidManifest.xml file if they are not already present:
```
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
// If you are not targeting the Google Play Store, you need to add the following permission:
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
// If you are targeting Android 13 and above (API level 33), you need to add the com.google.android.gms.AD_ID permission to read the device's advertising ID.
<uses-permission android:name="com.google.android.gms.permission.AD_ID"/>
```

3. Add the dependency under ```dependencies```
```
// RudderStack Android-SDK 
implementation 'com.rudderstack.android.sdk:core:[1.0,2.0)'
// RudderStack Adjust-SDK
implementation 'com.rudderstack.android.integration:adjust:1.0.1'
// Add Google Play Services library to enable the Google Advertising ID for Adjust SDK
implementation 'com.google.android.gms:play-services-ads-identifier:17.0.1'
// To support the Google Play Referrer API, make sure you have the following in your build.gradle file:
implementation 'com.android.installreferrer:installreferrer:2.2'
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
