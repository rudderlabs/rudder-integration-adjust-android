package com.rudderstack.android.sample.kotlin

import android.app.Application
import com.rudderstack.android.integration.adjust.AdjustIntegrationFactory
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger


class MainApplication : Application() {
    companion object {
        lateinit var rudderClient: RudderClient
        const val WRITE_KEY = ""
        const val DATA_PLANE_URL = ""
    }

    override fun onCreate() {
        super.onCreate()
        rudderClient = RudderClient.getInstance(
            this,
            WRITE_KEY,
            RudderConfig.Builder()
                .withDataPlaneUrl(DATA_PLANE_URL)
                .withLogLevel(RudderLogger.RudderLogLevel.DEBUG)
                .withFactory(AdjustIntegrationFactory.FACTORY)
                .build()
        )
    }
}
