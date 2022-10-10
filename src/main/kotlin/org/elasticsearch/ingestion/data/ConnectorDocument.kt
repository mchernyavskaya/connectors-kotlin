package org.elasticsearch.ingestion.data

/**
 * Elastic document that a connector ingests
 */
data class ConnectorDocument(
    val id: String,
    val title: String,
    val content: String,
    val url: String,
    val createdAt: Long,
    val updatedAt: Long? = null
)
