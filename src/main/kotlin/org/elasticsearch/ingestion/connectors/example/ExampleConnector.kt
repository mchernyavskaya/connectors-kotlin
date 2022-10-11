package org.elasticsearch.ingestion.connectors.example

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.elasticsearch.ingestion.connectors.base.ConfigurableField
import org.elasticsearch.ingestion.connectors.base.Connector
import org.elasticsearch.ingestion.connectors.data.ConnectorConfig
import org.elasticsearch.ingestion.connectors.data.ConnectorDocument

class ExampleConnector(configuration: ConnectorConfig) : Connector(configuration) {
    override fun displayName(): String {
        return "Example Kotlin Connector"
    }

    override fun configurableFields(): List<ConfigurableField> {
        return listOf(
            ConfigurableField("Example Field", "foo", "bar")
        )
    }

    override fun serviceType(): String {
        return "exampleKotlin"
    }

    override fun fetchDocuments(): Flow<ConnectorDocument> {
        return flow {
            1.rangeTo(10).forEach {
                emit(createStubDocument(it))
            }
        }
    }

    override fun doHealthCheck() {
        // do nothing
        logger.info("Health check passed for connector ${displayName()}")
    }

    /**
     * Simulating some content
     */
    private fun createStubDocument(index: Int) = ConnectorDocument(
        index.toString(),
        "Example Document $index",
        "$index - This is an example document. Treat it as such.",
        "https://example.com/${index}",
        0
    )
}
