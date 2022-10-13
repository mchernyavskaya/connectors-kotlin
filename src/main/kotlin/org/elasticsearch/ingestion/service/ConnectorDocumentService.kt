package org.elasticsearch.ingestion.service

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.FieldSort
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.Time
import co.elastic.clients.elasticsearch.core.BulkRequest
import co.elastic.clients.elasticsearch.core.SearchRequest
import mu.KLogging
import org.elasticsearch.ingestion.data.ConnectorDocument
import org.springframework.stereotype.Service

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

    fun getDocumentIds(indexName: String): List<String> {
        val pageSize = 1000
        val result = mutableListOf<String>()
        val requestBuilder = SearchRequest.Builder()
            .size(pageSize)
            .sort { sort ->
                sort.field(FieldSort.of {
                    it.field("id")
                    it.order(SortOrder.Asc)
                })
            }
        val opResponse = elasticClient.openPointInTime {
            it.index(indexName)
            it.keepAlive(Time.of { t -> t.time("1m") })
        }
        try {
            var searchRequest = requestBuilder.pit { pit -> pit.id(opResponse.id()) }.build()
            var searchResponse = elasticClient.search(searchRequest, ConnectorDocument::class.java)
            var hits = searchResponse.hits()?.hits()
            while (hits != null && hits.isNotEmpty()) {
                result.addAll(hits.map { hit -> hit.id() })
                searchRequest = requestBuilder.searchAfter(hits.last().sort()).build()
                searchResponse = elasticClient.search(searchRequest, ConnectorDocument::class.java)
                hits = searchResponse.hits()?.hits()
            }
        } finally {
            elasticClient.closePointInTime() {
                it.id(opResponse.id())
            }
        }
        return result
    }

    fun ensureMappingsExist(indexName: String) {
        elasticClient.indices().getMapping { it.index(indexName) }.also { mappingResponse ->
            if (mappingResponse[indexName]?.mappings()?.properties()?.isEmpty() != false) {
                logger.info("Creating mappings for index $indexName...")
                this.javaClass.getResource("/content_index_mappings.json")?.openStream().use { stream ->
                    elasticClient.indices().putMapping {
                        it.index(indexName)
                        it.withJson(stream)
                    }
                }
            } else {
                logger.info("Index $indexName mappings already exist: ${mappingResponse.result()}")
            }
        }
    }

    companion object : KLogging()
}
