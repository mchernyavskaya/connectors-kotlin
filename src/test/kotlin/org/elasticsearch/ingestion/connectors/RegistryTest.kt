package org.elasticsearch.ingestion.connectors

import org.amshove.kluent.internal.assertFailsWith
import org.elasticsearch.ingestion.connectors.example.ExampleConnector
import org.elasticsearch.ingestion.service.Registry
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class RegistryTest {

    @Test
    fun `finds connector class when exists`() {
        val clazz = Registry.connectorClass("exampleKotlin")
        assertEquals(ExampleConnector::class, clazz)
    }

    @Test
    fun `throws exception when connector class does not exist`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            Registry.connectorClass("doesNotExist")
        }
        assertEquals("No connector class found for service doesNotExist", exception.message)
    }
}
