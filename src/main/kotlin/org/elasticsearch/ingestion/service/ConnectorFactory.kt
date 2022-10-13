package org.elasticsearch.ingestion.service

import org.elasticsearch.ingestion.connectors.base.Connector
import org.elasticsearch.ingestion.data.ConnectorConfig
import org.springframework.stereotype.Component

@Suppress("UNCHECKED_CAST")
@Component
class ConnectorFactory {
    private val connectors = mutableMapOf<String, Connector>()

    fun createConnector(connectorConfig: ConnectorConfig): Connector {
        val connector = Registry.connectorClass(connectorConfig.serviceType!!)
            .constructors
            .first()
            .call(connectorConfig)
        connectors[createId(connectorConfig)] = connector
        return connector
    }

    private fun createId(connectorConfig: ConnectorConfig): String {
        return "${connectorConfig.serviceType}-${connectorConfig.id}"
    }
}
