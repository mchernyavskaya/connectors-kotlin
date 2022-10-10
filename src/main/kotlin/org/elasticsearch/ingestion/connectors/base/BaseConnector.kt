package org.elasticsearch.ingestion.connectors.base

import org.elasticsearch.ingestion.exception.ConnectorException
import org.elasticsearch.ingestion.exception.HealthCheckException
import org.elasticsearch.ingestion.data.ConnectorDocument
import kotlinx.coroutines.flow.Flow
import mu.KLogging

// we want this to have a type field later
data class ConfigurableField(
    val label: String,
    val name: String,
    val defaultValue: Any? = null
) {
    // if the label isn't specified, it defaults to field name
    constructor(name: String, defaultValue: Any? = null) : this(name, name, defaultValue.toString())
}

open class BaseConnector {
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
