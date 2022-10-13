package org.elasticsearch.ingestion.service

import org.elasticsearch.ingestion.connectors.base.ConfigurableField
import org.elasticsearch.ingestion.data.*
import org.springframework.stereotype.Service
import java.util.*

@Service
class ConnectorConfigService(private val repository: ConnectorRepository) {
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

    fun updateConnectorConfiguration(id: String, configurableFields: List<ConfigurableField>): ConnectorConfig {
        val connector = repository.findById(id).get()
        connector.configuration = configurableFields.associate {
            it.name to ConfigurationItem(it.label, "${it.defaultValue}")
        }
        connector.status = ConnectorStatus.configured
        connector.lastSeen = Date()
        // if not all fields have defaults, we still need to configure
        configurableFields.find { it.defaultValue == null }?.let {
            connector.status = ConnectorStatus.needs_configuration
        }
        return repository.save(connector)
    }

    fun markConnectorSyncStarted(id: String): ConnectorConfig {
        val connector = repository.findById(id).get()
        connector.syncNow = false
        connector.lastSynced = Date()
        connector.lastSyncStatus = SyncStatus.in_progress
        return repository.save(connector)
    }

    fun markConnectorSyncCompleted(
        id: String,
        status: SyncStatus,
        indexedCount: Long = 0,
        deletedCount: Long = 0,
        errorMessage: String? = null
    ): ConnectorConfig {
        val connector = repository.findById(id).get()
        connector.lastSyncStatus = status
        connector.lastSyncError = errorMessage
        connector.lastIndexedDocumentCount = indexedCount
        connector.lastDeletedDocumentCount = deletedCount
        return repository.save(connector)
    }

    fun findConnectorConfig(connectorId: String): ConnectorConfig? = repository.findById(connectorId).orElse(null)
}
