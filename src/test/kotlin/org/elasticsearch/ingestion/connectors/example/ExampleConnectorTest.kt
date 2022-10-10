package org.elasticsearch.ingestion.connectors.example

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

@ExperimentalCoroutinesApi
internal class ExampleConnectorTest {
    private val exampleConnector: ExampleConnector = ExampleConnector()

    @Test
    fun displayName() {
        assert(exampleConnector.displayName() == "Example Connector")
    }

    @Test
    fun configurableFields() {
        assert(exampleConnector.configurableFields().size == 1)
        assert(exampleConnector.configurableFields()[0].name == "foo")
        assert(exampleConnector.configurableFields()[0].defaultValue == "bar")
    }

    @Test
    fun serviceType() {
        assert(exampleConnector.serviceType() == "example")
    }

    @Test
    fun fetchDocuments(): Unit = runTest {
        val documents = exampleConnector.fetchDocuments().toList(mutableListOf())
        assert(documents.size == 10)
        assert(documents[0].id == "1")
        assert(documents[0].title == "Example Document 1")
        assert(documents[0].content == "1 - This is an example document. Treat it as such.")
        assert(documents[0].url == "https://example.com/1")
    }

    @Test
    fun doHealthCheck() {
        assertDoesNotThrow {
            exampleConnector.doHealthCheck()
        }
    }
}
