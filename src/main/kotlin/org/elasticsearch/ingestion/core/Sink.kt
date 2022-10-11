package org.elasticsearch.ingestion.core

import mu.KLogging
import org.elasticsearch.ingestion.connectors.data.ConnectorDocument
import org.elasticsearch.ingestion.connectors.data.ConnectorDocumentService

interface Sink {
    fun ingest(document: ConnectorDocument)
    fun ingestMultiple(documents: List<ConnectorDocument>)
    fun delete(id: String)
    fun deleteMultiple(ids: List<String>)
}

class SinkException(message: String) : Exception(message) {
    constructor(message: String, cause: Throwable) : this(message) {
        initCause(cause)
    }
}

/**
 * This sink is used for testing purposes only
 */
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

class ElasticSink(private val indexName: String, private val service: ConnectorDocumentService) : Sink {
    override fun ingest(document: ConnectorDocument) {
        service.indexDocument(indexName, document)
    }

    override fun ingestMultiple(documents: List<ConnectorDocument>) {
        service.indexDocuments(indexName, documents)
    }

    override fun delete(id: String) {
        service.deleteDocument(indexName, id)
    }

    override fun deleteMultiple(ids: List<String>) {
        service.deleteDocuments(indexName, ids)
    }
}
