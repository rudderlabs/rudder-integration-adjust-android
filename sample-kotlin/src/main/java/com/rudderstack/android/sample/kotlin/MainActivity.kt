package com.rudderstack.android.sample.kotlin

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.rudderstack.android.sdk.core.RudderProperty
import com.rudderstack.android.sdk.core.RudderTraits

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.track)?.setOnClickListener { track() }
        findViewById<Button>(R.id.identify)?.setOnClickListener { identify() }
        findViewById<Button>(R.id.reset)?.setOnClickListener { reset() }
    }

    private fun track() {
        MainApplication.rudderClient.track(
            "Order Completed",
            RudderProperty()
                .putValue("category", "test_category")
                .putValue("key-1", "value-1")
                .putValue("key-2", 20.39)
                .putValue("key-3", true)
                .putValue("key-4", 1234567890)
        )
    }

    private fun identify() {
        MainApplication.rudderClient.identify("Android userId",
            RudderTraits()
                .putFirstName("John"),
            null
        )
    }

    private fun reset() {
        MainApplication.rudderClient.reset()
    }
}
