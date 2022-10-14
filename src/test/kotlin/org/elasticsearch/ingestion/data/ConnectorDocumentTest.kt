package org.elasticsearch.ingestion.data

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.util.*

internal class ConnectorDocumentTest {

    private val doc = createStubDocument()
    private val mapper = ObjectMapper().registerModule(kotlinModule())

    @Test
    fun `check object mapper serialization and deserialization`() {
        val json = mapper.writeValueAsString(doc)
        println(json)
        val read = mapper.readValue<ConnectorDocument>(json)
        read shouldBeEqualTo doc
    }

    private fun createStubDocument(): ConnectorDocument {
        return ConnectorDocument(
            id = "id-1",
            title = "title-1",
            content = "content-1",
            url = "http://www.example.com/1",
            createdAt = Date(),
            updatedAt = Date()
        )
    }
}
