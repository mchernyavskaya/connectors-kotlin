package org.elasticsearch.ingestion.connectors.base

import kotlinx.coroutines.flow.Flow
import mu.KLogging
import org.elasticsearch.ingestion.data.ConnectorConfig
import org.elasticsearch.ingestion.data.ConnectorDocument
import org.elasticsearch.ingestion.data.ConnectorStatus
import org.elasticsearch.ingestion.service.ConnectorException
import org.elasticsearch.ingestion.service.HealthCheckException
import org.joda.time.DateTime
import org.quartz.CronExpression
import java.time.Instant
import java.util.*

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

    fun indexName() = configuration.indexName

    fun lastSeen() = configuration.lastSeen

    fun shouldSync(): Boolean {
        if (configuration.isSyncing() || !configuration.isSyncEnabled()) {
            return false
        }
        if (configuration.syncNow || configuration.lastSyncFailed()) {
            return true
        }
        val exp = CronExpression(configuration.scheduling?.interval)
        val past = DateTime.now().minusDays(1).toDate()
        val timeAfter = if (configuration.lastSynced == null) past else DateTime(configuration.lastSynced).toDate()
        return exp.getNextValidTimeAfter(timeAfter).before(Date())
    }

    fun shouldConfigure(): Boolean {
        // TODO make this on par with connectors-ruby
        return configuration.status == ConnectorStatus.created
    }

    fun shouldHeartbeat(heartbeatInterval: Long): Boolean {
        if (!configuration.statusAllowsSync()) {
            return false
        }
        return configuration.lastSynced == null ||
                configuration.lastSynced!!.toInstant().plusMillis(heartbeatInterval).isBefore(Instant.now())
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
