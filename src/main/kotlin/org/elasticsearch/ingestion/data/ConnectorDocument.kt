package org.elasticsearch.ingestion.data

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Field
import java.util.*


/**
 * Elastic document that a connector ingests
 */
data class ConnectorDocument(
    @Id
    val id: String,
    val title: String,
    val content: String,
    val url: String,
    @Field(name = "created_at")
    val createdAt: Date = Date(),
    @Field(name = "updated_at")
    val updatedAt: Date? = null
)
