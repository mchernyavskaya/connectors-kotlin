package org.elasticsearch.ingestion.data

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.annotations.WriteTypeHint
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import java.util.*

interface ConnectorJobRepository : ElasticsearchRepository<ConnectorJob, String> {
    fun findByConnectorIdAndStatus(connectorId: String, status: SyncStatus): ConnectorJob?
}

@Document(indexName = ".elastic-connectors-sync-jobs", writeTypeHint = WriteTypeHint.FALSE)
data class ConnectorJob(
    @Id
    val id: String? = null, // for new records it is empty
    @Field("completed_at", type = FieldType.Date)
    var completedAt: Date? = null,
    @Field("created_at", type = FieldType.Date)
    val createdAt: Date = Date(),
    @Field("connector_id")
    var connectorId: String? = null,
    @Field("deleted_document_count")
    var deletedDocumentCount: Long? = null,
    @Field("indexed_document_count")
    var indexedDocumentCount: Long? = null,
    var error: String? = null,
    var status: SyncStatus? = null,
    val workerHostname: String? = null,
    var connector: ConnectorConfig? = null
)
