package com.rudderstack.android.integration.adjust

import com.adjust.sdk.AdjustInstance
import com.google.gson.GsonBuilder
import com.rudderstack.android.sdk.core.RudderMessage
import junit.framework.TestCase.assertEquals
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito
import org.powermock.api.mockito.PowerMockito
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader


class AdjustIntegrationFactoryTest {
    var adjustIntegrationFactory : AdjustIntegrationFactory? = null

    private val TEST_JSON_PATH_MAP = mapOf(
        "identify_input_1.json" to "identify_output_1.json"
    )

    @Mock
    var adjustIntegration : AdjustInstance? = null
    val gson = GsonBuilder().create()
    @Before
    fun setUp() {
        val configJson = getJsonFromPath("destinationConfig.json")
            ?: throw Exception("Config json is null")
        val configObject = gson.fromJson(configJson, Map::class.java)
        adjustIntegrationFactory = AdjustIntegrationFactory(adjustIntegration, configObject)
    }

    fun getJsonFromPath(path: String?): String? {
        val inputStream: InputStream = this.javaClass.classLoader?.getResourceAsStream(path) ?: return null
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
        val firstInput = TEST_JSON_PATH_MAP.keys.toTypedArray()[0]
        MatcherAssert.assertThat(firstInput, Matchers.notNullValue())
        val inputStream = javaClass.classLoader.getResourceAsStream(firstInput)
        MatcherAssert.assertThat(inputStream, Matchers.notNullValue())
    }


    @Test
    @Throws(java.lang.Exception::class)
    fun testEvents() {
        for ((inputJsonPath, outputJsonPath) in TEST_JSON_PATH_MAP) {

//        for ((key, value): Map.Entry<String?, String?> in TEST_JSON_PATH_MAP) {
            val emailCaptor = ArgumentCaptor.forClass(
                String::class.java
            )
            val firstNameCaptor = ArgumentCaptor.forClass(
                String::class.java
            )
            val lastNameCaptor = ArgumentCaptor.forClass(
                String::class.java
            )
            val phoneCaptor = ArgumentCaptor.forClass(
                String::class.java
            )
            val dateOfBirthCaptor = ArgumentCaptor.forClass(
                String::class.java
            )
            val genderCaptor = ArgumentCaptor.forClass(
                String::class.java
            )
            val cityCaptor = ArgumentCaptor.forClass(
                String::class.java
            )
            val stateCaptor = ArgumentCaptor.forClass(
                String::class.java
            )
            val zipCaptor = ArgumentCaptor.forClass(
                String::class.java
            )
            val countryCaptor = ArgumentCaptor.forClass(
                String::class.java
            )
            PowerMockito.doNothing().`when`<Class<AppEventsLogger>>(
                AppEventsLogger::class.java, "setUserID",
                userIdCaptor.capture()
            ) /*.thenAnswer((Answer<Void>) invocation -> Void.TYPE.newInstance())*/
            PowerMockito.doNothing().`when`<Class<AppEventsLogger>>(
                AppEventsLogger::class.java, "setUserData",
                emailCaptor.capture(),
                firstNameCaptor.capture(),
                lastNameCaptor.capture(),
                phoneCaptor.capture(),
                dateOfBirthCaptor.capture(),
                genderCaptor.capture(),
                cityCaptor.capture(),
                stateCaptor.capture(),
                zipCaptor.capture(),
                countryCaptor.capture()
            ) /*.thenAnswer((Answer<Void>) invocation -> Void.TYPE.newInstance())*/
            testMyIO.test(
                key,
                value, RudderMessage::class.java,
                TestOutput::class.java, Operation<I, O> { input: I? ->
                    facebookIntegrationFactory.dump(input)
                    TestOutput(
                        userIdCaptor.getValue(),
                        Traits(
                            emailCaptor.value,
                            firstNameCaptor.value,
                            lastNameCaptor.value,
                            phoneCaptor.value,
                            dateOfBirthCaptor.value,
                            genderCaptor.value,
                            cityCaptor.value,
                            countryCaptor.value,
                            zipCaptor.value,
                            stateCaptor.value
                        )
                    )
                })
        }
    }



}