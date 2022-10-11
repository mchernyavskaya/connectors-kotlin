package org.elasticsearch.ingestion.connectors.data

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.core.BulkRequest
import org.springframework.stereotype.Service

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

@Service
class ConnectorDocumentService(private val elasticClient: ElasticsearchClient) {
    fun indexDocument(indexName: String, document: ConnectorDocument) {
        elasticClient.index {
            it.index(indexName)
            it.document(document)
        }
    }

    fun indexDocuments(indexName: String, documents: List<ConnectorDocument>) {
        val requestBuilder = BulkRequest.Builder().index(indexName)
        documents.forEach { document ->
            requestBuilder.operations { op ->
                op.index {
                    it.document(document)
                }
            }
        }
        elasticClient.bulk(requestBuilder.build())
    }

    fun deleteDocument(indexName: String, id: String) {
        elasticClient.delete {
            it.index(indexName)
            it.id(id)
        }
    }

    fun deleteDocuments(indexName: String, ids: List<String>) {
        val requestBuilder = BulkRequest.Builder().index(indexName)
        ids.forEach { id ->
            requestBuilder.operations { op ->
                op.delete {
                    it.id(id)
                }
            }
        }
        elasticClient.bulk(requestBuilder.build())
    }
}

