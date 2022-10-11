package org.elasticsearch.ingestion.connectors

import org.elasticsearch.ingestion.connectors.data.ConnectorConfig
import org.elasticsearch.ingestion.connectors.data.ConnectorRepository
import org.elasticsearch.ingestion.connectors.data.ConnectorStatus
import org.springframework.stereotype.Service

@Service
class ElasticConnectorService(private val repository: ConnectorRepository) {
    fun connectorConfiguration(id: String): ConnectorConfig? {
        return repository.findById(id).orElse(null)
    }

    fun updateConnectorStatus(id: String, status: ConnectorStatus, errorMessage: String? = null): ConnectorConfig {
        val connector = repository.findById(id).get()
        connector.status = status
        if (status == ConnectorStatus.error) {
            if (errorMessage != null) {
                connector.error = errorMessage
            } else {
                throw IllegalArgumentException("Error message must be provided when updating connector status to error")
            }
        } else {
            connector.error = null
        }
        return repository.save(connector)
    }

    fun updateConnectorServiceType(id: String, serviceType: String): ConnectorConfig {
        val connector = repository.findById(id).get()
        connector.serviceType = serviceType
        return repository.save(connector)
    }

    fun findConnectorConfig(connectorId: String): ConnectorConfig? = repository.findById(connectorId).orElse(null)
}
