package com.rudderstack.android.integration.adjust

import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustEvent
import com.adjust.sdk.AdjustInstance
import com.google.gson.GsonBuilder
import com.rudderstack.android.sdk.core.RudderMessage
import com.rudderstack.android.test.testio.TestMyIO
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader


@RunWith(PowerMockRunner::class)
@PrepareForTest(
    Adjust::class,
    AdjustIntegrationFactory::class,
    AdjustIntegrationFactoryTest::class
)
class AdjustIntegrationFactoryTest {
    var adjustIntegrationFactory: AdjustIntegrationFactory? = null

    private val TEST_IDENTIFY_JSON_PATH_MAP_1 = mapOf(
        "identify_input_1.json" to "identify_output_1.json"
    )

    @Mock
    var adjustIntegration: AdjustInstance? = null
    val gson = GsonBuilder().create()
    var closeable: AutoCloseable? = null

    @Before
    fun setUp() {
        closeable = MockitoAnnotations.openMocks(this)
        PowerMockito.spy(Adjust::class.java)
        val configJson = getJsonFromPath("destinationConfig.json")
            ?: throw Exception("Config json is null")
        val configObject = gson.fromJson(configJson, Map::class.java)
        adjustIntegrationFactory = AdjustIntegrationFactory(adjustIntegration, configObject)
    }

    @After
    fun destroy() {
        closeable?.close()
    }

    fun getJsonFromPath(path: String?): String? {
        val inputStream: InputStream =
            this.javaClass.classLoader?.getResourceAsStream(path) ?: return null
        val reader = BufferedReader(InputStreamReader(inputStream))
        val builder = StringBuilder()
        var line: String?
        try {
            while (reader.readLine().also { line = it } != null) {
                builder.append(line)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return builder.toString()
    }

    @Test
    fun assertJsonsAreReadCorrectly() {
        val firstInput = TEST_IDENTIFY_JSON_PATH_MAP_1.keys.toTypedArray()[0]
        MatcherAssert.assertThat(firstInput, Matchers.notNullValue())
        val inputStream = javaClass.classLoader?.getResourceAsStream(firstInput)
        MatcherAssert.assertThat(inputStream, Matchers.notNullValue())
    }

    private val testMyIO = TestMyIO(javaClass.classLoader)

    @Test
    @Throws(java.lang.Exception::class)
    fun identify() {
        for ((inputJsonPath, outputJsonPath) in TEST_IDENTIFY_JSON_PATH_MAP_1) {
            val identifyIdCaptor = ArgumentCaptor.forClass(String::class.java)

            PowerMockito.doNothing().`when`(
                Adjust::class.java, "addSessionPartnerParameter",
                Mockito.anyString(), identifyIdCaptor.capture()
            )

            testMyIO.test(
                inputJsonPath, outputJsonPath,
                RudderMessage::class.java,
                TestIdentify::class.java
            ) { input ->
                adjustIntegrationFactory?.dump(input)
                TestIdentify(
                    identifyIdCaptor.allValues
                )
            }
        }
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun customTrack() {
        trackTest("customTrack_input_1.json", "customTrack_output_1.json")
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun orderCompletedWithRevenueAndCurrency() {
        trackTest("orderCompletedTrack_input_2.json", "orderCompletedTrack_output_2.json")
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun orderCompletedWithOnlyRevenue() {
        trackTest("orderCompletedTrack_input_3.json", "orderCompletedTrack_output_3.json")
    }

    private fun trackTest(inputJsonPath: String, outputJsonPath: String) {
        val identifyIdCaptor = ArgumentCaptor.forClass(String::class.java)
        val adjustEventCaptor = ArgumentCaptor.forClass(AdjustEvent::class.java)

        PowerMockito.doNothing().`when`(
            Adjust::class.java, "addSessionPartnerParameter",
            Mockito.anyString(), identifyIdCaptor.capture()
        )

        PowerMockito.doNothing().`when`(
            adjustIntegration, "trackEvent",
            adjustEventCaptor.capture()
        )

        testMyIO.test(
            inputJsonPath, outputJsonPath,
            RudderMessage::class.java,
            TestTrack::class.java
        ) { input ->
            adjustIntegrationFactory?.dump(input)
            val adjustEvent = adjustEventCaptor.allValues
            println(adjustEvent)
            TestTrack(
                identifyIdCaptor.allValues,
                adjustEventCaptor.allValues
            )
        }
    }
}