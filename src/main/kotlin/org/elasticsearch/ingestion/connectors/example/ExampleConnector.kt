package org.elasticsearch.ingestion.connectors.example

import org.elasticsearch.ingestion.connectors.base.BaseConnector
import org.elasticsearch.ingestion.connectors.base.ConfigurableField
import org.elasticsearch.ingestion.data.ConnectorDocument
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.stereotype.Component

@Component
class ExampleConnector : BaseConnector() {
    override fun displayName(): String {
        return "Example Connector"
    }

    override fun configurableFields(): List<ConfigurableField> {
        return listOf(
            ConfigurableField("Example Field", "foo", "bar")
        )
    }

    override fun serviceType(): String {
        return "example"
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
