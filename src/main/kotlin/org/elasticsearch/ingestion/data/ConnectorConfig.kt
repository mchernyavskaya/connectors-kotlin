@file:Suppress("EnumEntryName")

package org.elasticsearch.ingestion.data

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.annotations.WriteTypeHint
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import java.util.*

interface ConnectorRepository : ElasticsearchRepository<ConnectorConfig, String>

data class ConfigurationItem(
    val label: String,
    val value: String? = null
)

data class PipelineConfig(
    @Field("extract_binary_content")
    val extractBinaryContent: Boolean, // Whether or not the `request_pipeline` should handle binary data
    val name: String, //  Ingest pipeline to utilize on indexing data to Elasticsearch
    @Field("reduce_whitespace")
    val reduceWhitespace: Boolean, // Whether or not the `request_pipeline` should squish redundant whitespace
    @Field("run_ml_inference")
    val runMlInference: Boolean // Whether or not the `request_pipeline` should run the ML Inference pipeline
)

data class SchedulingConfig(
    val enabled: Boolean, // Is sync schedule enabled?
    val interval: String // Quartz Cron syntax
)

@Document(indexName = ".elastic-connectors", writeTypeHint = WriteTypeHint.FALSE)
data class ConnectorConfig(
    @Id
    val id: String? = null, // for new records it is empty
    @Field("api_key_id")
    val apiKeyId: String? = null, // ID of the current API key in use
    // Definition and values of configurable
    var configuration: Map<String, ConfigurationItem> = mutableMapOf(),
    var error: String? = null, //  Optional error message
    @Field("index_name")
    val indexName: String, // The index data will be written to
    val language: String? = null, // the language used for the analyzer
    @Field("last_seen", type = FieldType.Date)
    var lastSeen: Date? = null, // Connector writes check-in date-time regularly (UTC)
    @Field("last_sync_error")
    var lastSyncError: String? = null, // Optional last sync error message
    @Field("last_sync_status")
    var lastSyncStatus: SyncStatus? = null, // last sync Enum, see below
    @Field("last_synced", type = FieldType.Date)
    var lastSynced: Date? = null, // Date/time of last completed sync (UTC)
    val name: String, // the name to use for the connector
    val scheduling: SchedulingConfig? = null,
    @Field("service_type")
    var serviceType: String? = null, // Used to map to the correct service
    var status: ConnectorStatus, // Enum, see below
    @Field("sync_now")
    var syncNow: Boolean = false, // Flag to signal user wants to initiate a sync
    @Field("last_indexed_document_count")
    var lastIndexedDocumentCount: Long? = null, // How many documents were inserted into the index
    @Field("last_deleted_document_count")
    var lastDeletedDocumentCount: Long? = null, // How many documents were deleted from the index
    @Field("is_native")
    val native: Boolean = false // Flag to signal a native connector
) {
    fun isSyncing(): Boolean {
        return lastSyncStatus != null && lastSyncStatus == SyncStatus.in_progress
    }

    fun lastSyncFailed(): Boolean {
        return lastSyncStatus != null && lastSyncStatus == SyncStatus.error
    }

    fun isSyncEnabled(): Boolean {
        return scheduling != null && scheduling!!.enabled
    }

    fun statusAllowsSync(): Boolean {
        return status in listOf(ConnectorStatus.error, ConnectorStatus.connected, ConnectorStatus.configured)
    }
}

enum class ConnectorStatus {
    created, // entry has been created but connector has not connected to elasticsearch (written by index creator)
    needs_configuration, // connector has written its configuration to elasticsearch (written by connector)
    configured, // connector has been fully configured (written by kibana on updating configuration, or directly by connector if no further configuration is necessary)
    connected, // connector has successfully connected to the data source (written by connector on successfully connecting to data source )
    error
}

enum class SyncStatus {
    in_progress, // sync successfully started
    completed, // sync successfully completed
    error // sync error
}

