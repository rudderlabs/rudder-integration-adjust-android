package com.rudderstack.android.sample.kotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rudderstack.android.sdk.core.RudderProperty

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sendEvents()
    }

    private fun sendEvents() {
        MainApplication.rudderClient.identify("RudderStack Android user id")

        MainApplication.rudderClient.track(
            "Track event 1",
            RudderProperty()
                .putValue("key1", "value1")
                .putValue("key2", 123)
                .putValue("key3", true)
                .putValue("key4", 4.56)

                .putValue("revenue", 4.99)
                .putValue("currency", "USD")
        )
    }
}
