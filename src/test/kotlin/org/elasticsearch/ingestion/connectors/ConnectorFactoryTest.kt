package org.elasticsearch.ingestion.connectors

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.elasticsearch.ingestion.connectors.data.ConnectorConfig
import org.elasticsearch.ingestion.connectors.example.ExampleConnector
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ConnectorFactoryTest {
    private val factory = ConnectorFactory()
    private val config = mockk<ConnectorConfig>() {
        every { serviceType } returns "exampleKotlin"
        every { id } returns "123"
    }

    @BeforeEach
    fun setUp() {
        mockkObject(Registry)
        every { Registry.connectorClass(any()) } returns ExampleConnector::class
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(Registry)
    }

    @Test
    fun createConnector() {
        val connector = factory.createConnector(config)
        connector shouldBeInstanceOf ExampleConnector::class
        connector.id() shouldBeEqualTo "123"
    }
}

