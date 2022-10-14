package org.elasticsearch.ingestion.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.elasticsearch.ingestion.connectors.base.Connector
import org.elasticsearch.ingestion.core.Sink
import org.elasticsearch.ingestion.data.ConnectorStatus
import org.elasticsearch.ingestion.service.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class ConnectorJobRunner(
    private val connectorProperties: ConnectorProperties,
    private val runnerProperties: RunnerProperties,
    private val connectorFactory: ConnectorFactory,
    private val connectorService: ConnectorConfigService,
    private val connectorJobService: ConnectorJobService,
    private val documentService: ConnectorDocumentService,
    @Qualifier("elasticSink")
    private val sink: Sink
) {
    private var stopped: Boolean = false

    fun runLoop() {
        logger.info("Starting job runner loop...")
        while (!stopped) {
            try {
                connectorService.findConnectorConfig(connectorProperties.id!!)?.let { config ->
                    logger.info("Current connector status is [${config.status}]")
                    val connector = connectorFactory.createConnector(config)
                    if (connector.shouldConfigure()) {
                        logger.info("Connector ${config.id} is not configured. Configuring...")
                        connectorService.updateConnectorConfiguration(config.id!!, connector.configurableFields())
                    }
                    if (connector.shouldSync()) {
                        logger.info("Running sync for connector: ${connector.id()}")
                        SyncJob(connector, sink, connectorJobService, documentService).run()
                    } else {
                        logger.info("Skipping sync for connector: ${connector.id()} (either not enabled or not time to sync)")
                    }
                }
            } catch (e: Exception) {
                logger.error("Error running connector job runner", e)
                connectorService.updateConnectorStatus(connectorProperties.id!!, ConnectorStatus.error, e.message)
                e.printStackTrace()
            }
            logger.info("Sleeping for ${runnerProperties.pollingInterval} ms...")
            TimeUnit.MILLISECONDS.sleep(runnerProperties.pollingInterval)
        }
    }

    fun stop() {
        stopped = true
    }

    companion object : KLogging()
}

class SyncJob(
    private val connector: Connector,
    private val sink: Sink,
    private val connectorJobService: ConnectorJobService,
    private val documentService: ConnectorDocumentService
) {
    fun run() = runBlocking {
        val job = connectorJobService.claimJob(connector.id()!!)
        val deletedIds = mutableSetOf<String>()
        val indexedIds = mutableSetOf<String>()
        launch(Dispatchers.Default) {
            // TODO move this to the preflight or configure step
            documentService.ensureMappingsExist(connector.indexName())
            val existingIds = documentService.getDocumentIds(connector.indexName())
            connector.fetchDocuments().collect { document ->
                sink.ingest(document)
                indexedIds.add(document.id)
                logger.info("Ingested document ${document.id}...")
            }
            val idsToDelete = existingIds - indexedIds
            logger.info("Deleting ${idsToDelete.size} documents: [${idsToDelete.joinToString(",")}]...")
            sink.deleteMultiple(idsToDelete)
        }.invokeOnCompletion { e ->
            if (e == null) {
                logger.info("Finished syncing connector [${connector.id()}]")
            } else {
                logger.error("Error syncing connector [${connector.id()}]", e)
            }
            connectorJobService.completeJob(job!!, indexedIds.size.toLong(), deletedIds.size.toLong(), e?.message)
            sink.flush()
        }
    }

    companion object : KLogging()
}

class ConfigurationJob(private val connector: Connector, private val service: ConnectorConfigService) {
    fun run() = runBlocking {
        launch(Dispatchers.Default) {

        }.invokeOnCompletion {
            logger.info("Finished configuring connector ${connector.id()}.")
        }
    }

    companion object : KLogging()
}

