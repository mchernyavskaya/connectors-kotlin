package org.elasticsearch.ingestion.service

import org.elasticsearch.ingestion.data.Connector
import org.elasticsearch.ingestion.data.ConnectorRepository
import org.elasticsearch.ingestion.data.ConnectorStatus
import org.springframework.stereotype.Service

@Service
class ElasticConnectorService(private val repository: ConnectorRepository) {
    fun connectorConfiguration(id: String): Connector? {
        return repository.findById(id).orElse(null)
    }

    fun updateConnectorStatus(id: String, status: ConnectorStatus, errorMessage: String? = null) {
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
        repository.save(connector)
    }

    fun findConnectorPackages(): List<Connector> {
        return repository.findByNativeOrderByName(false)
    }
}
