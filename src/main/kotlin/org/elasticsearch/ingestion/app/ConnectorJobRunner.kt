package org.elasticsearch.ingestion.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.elasticsearch.ingestion.connectors.ConnectorFactory
import org.elasticsearch.ingestion.connectors.ElasticConnectorService
import org.elasticsearch.ingestion.connectors.base.Connector
import org.elasticsearch.ingestion.core.Sink
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class ConnectorJobRunner(
    private val connectorProperties: ConnectorProperties,
    private val runnerProperties: RunnerProperties,
    private val connectorFactory: ConnectorFactory,
    private val connectorService: ElasticConnectorService,
    @Qualifier("consoleSink") // TODO change to elastic sink
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
                        SyncJob(connector, sink).run()
                    } else {
                        logger.info("Skipping sync for connector: ${connector.id()} (either not enabled or not time to sync)")
                    }
                }
            } catch (e: Exception) {
                logger.error("Error running connector job runner", e)
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

class SyncJob(private val connector: Connector, private val sink: Sink) {
    fun run() = runBlocking {
        launch(Dispatchers.Default) {
            connector.fetchDocuments().collect { document ->
                sink.ingest(document)
                logger.info("Ingested document ${document.id}...")
            }
        }.invokeOnCompletion {
            logger.info("Finished syncing connector ${connector.id()}.")
            sink.flush()
        }
    }

    companion object : KLogging()
}

class ConfigurationJob(private val connector: Connector, private val service: ElasticConnectorService) {
    fun run() = runBlocking {
        launch(Dispatchers.Default) {

        }.invokeOnCompletion {
            logger.info("Finished configuring connector ${connector.id()}.")
        }
    }

    companion object : KLogging()
}

