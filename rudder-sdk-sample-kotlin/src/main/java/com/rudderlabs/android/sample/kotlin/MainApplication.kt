package com.rudderlabs.android.sample.kotlin

import android.app.Application
import com.rudderlabs.android.integration.adjust.AdjustIntegrationFactory
import com.rudderlabs.android.sdk.core.RudderClient
import com.rudderlabs.android.sdk.core.RudderConfig
import com.rudderlabs.android.sdk.core.RudderLogger

class MainApplication : Application() {
    companion object {
        private const val WRITE_KEY = "1SEkFBSRyXIUWmPoOpfcHiKEmOR"
        private const val END_POINT_URI = "https://d018f0e9.ngrok.io"
        lateinit var rudderClient: RudderClient
    }

    override fun onCreate() {
        super.onCreate()
        rudderClient = RudderClient.getInstance(
            this,
            WRITE_KEY,
            RudderConfig.Builder()
                .withEndPointUri(END_POINT_URI)
                .withLogLevel(4)
                .withFactory(AdjustIntegrationFactory.FACTORY)
                .build()
        )
    }
}