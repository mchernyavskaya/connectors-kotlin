package org.elasticsearch.ingestion.core

import mu.KLogging
import org.elasticsearch.ingestion.app.ConnectorProperties
import org.elasticsearch.ingestion.connectors.ElasticConnectorService
import org.elasticsearch.ingestion.connectors.data.ConnectorDocument
import org.elasticsearch.ingestion.connectors.data.ConnectorDocumentService
import org.springframework.stereotype.Component

interface Sink {
    fun ingest(document: ConnectorDocument)
    fun ingestMultiple(documents: List<ConnectorDocument>)
    fun delete(id: String)
    fun deleteMultiple(ids: List<String>)
    fun flush() {}
}

class SinkException(message: String) : Exception(message) {
    constructor(message: String, cause: Throwable) : this(message) {
        initCause(cause)
    }
}

/**
 * This sink is used for testing purposes only
 */
@Component("consoleSink")
class ConsoleSink : Sink {
    override fun ingest(document: ConnectorDocument) {
        logger.info("Ingesting document: $document")
    }

    override fun ingestMultiple(documents: List<ConnectorDocument>) {
        logger.info("Ingesting documents: $documents")
    }

    override fun delete(id: String) {
        logger.info("Deleting document with id: $id")
    }

    override fun deleteMultiple(ids: List<String>) {
        logger.info("Deleting documents with ids: $ids")
    }

    companion object : KLogging()
}

@Component("elasticSink")
class ElasticSink(
    connectorProperties: ConnectorProperties,
    connectorService: ElasticConnectorService,
    private val documentService: ConnectorDocumentService
) : Sink {
    private var indexName: String? = null

    init {
        logger.info("Initializing ElasticSink...")
        connectorService.findConnectorConfig(connectorProperties.id!!)?.let {
            // TODO: need to reload index name periodically
            // in case it's changed in the connector config
            indexName = it.indexName
        }
    }

    override fun ingest(document: ConnectorDocument) {
        documentService.indexDocument(indexName!!, document)
    }

    override fun ingestMultiple(documents: List<ConnectorDocument>) {
        documentService.indexDocuments(indexName!!, documents)
    }

    override fun delete(id: String) {
        documentService.deleteDocument(indexName!!, id)
    }

    override fun deleteMultiple(ids: List<String>) {
        documentService.deleteDocuments(indexName!!, ids)
    }

    companion object : KLogging()
}
