package org.elasticsearch.ingestion.connectors.base

import kotlinx.coroutines.flow.Flow
import mu.KLogging
import org.elasticsearch.ingestion.connectors.ConnectorException
import org.elasticsearch.ingestion.connectors.HealthCheckException
import org.elasticsearch.ingestion.connectors.data.ConnectorConfig
import org.elasticsearch.ingestion.connectors.data.ConnectorDocument

// we want this to have a type field later
data class ConfigurableField(
    val label: String,
    val name: String,
    val defaultValue: Any? = null
)

abstract class Connector(private val configuration: ConnectorConfig) {
    open fun displayName(): String {
        throw ConnectorException("Not implemented for this connector")
    }

    open fun configurableFields(): List<ConfigurableField> {
        throw ConnectorException("Not implemented for this connector")
    }

    open fun serviceType(): String {
        throw ConnectorException("Not implemented for this connector")
    }

    open fun fetchDocuments(): Flow<ConnectorDocument> {
        throw ConnectorException("Not implemented for this connector")
    }

    fun id() = configuration.id

    fun doHealthCheckAndRaise() {
        try {
            doHealthCheck()
        } catch (e: Exception) {
            logger.error("Health check failed for connector ${displayName()}", e)
            throw HealthCheckException("Health check failed for connector ${displayName()}", e)
        }
    }

    fun healthy(): Boolean {
        return try {
            doHealthCheck()
            true
        } catch (e: Exception) {
            logger.error("Health check failed for connector ${displayName()}", e)
            false
        }
    }

    open fun doHealthCheck() {
        throw ConnectorException("Not implemented for this connector")
    }

    companion object : KLogging()
}
