package org.elasticsearch.ingestion.app

import mu.KLogging
import org.elasticsearch.ingestion.service.ConnectorConfigService
import org.elasticsearch.ingestion.data.ConnectorConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.event.EventListener
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories

@SpringBootApplication
@ComponentScan(basePackages = ["org.elasticsearch.ingestion"])
@EnableElasticsearchRepositories(basePackages = ["org.elasticsearch.ingestion"])
@EnableConfigurationProperties(
    ConnectorProperties::class,
    RunnerProperties::class,
    ElasticClientProperties::class
)
class ConnectorApplication(
    private val service: ConnectorConfigService,
    private val properties: ConnectorProperties,
    private val jobRunner: ConnectorJobRunner
) {
    @EventListener(ApplicationReadyEvent::class)
    fun onStart() {
        logger.info("Starting ConnectorApplication...")
        if (properties.id.isNullOrEmpty()) {
            throw IllegalArgumentException("Connector ID is not set. Please create a connector to set the ID and restart.")
        }
        val config = service.findConnectorConfig(properties.id)
        if (config != null) {
            verifyAndUpdateServiceType(config)
        } else {
            throw IllegalArgumentException("Connector with ID ${properties.id} is not found. Please recreate it and restart the application with a new ID.")
        }
        jobRunner.runLoop()
    }

    private fun verifyAndUpdateServiceType(connectorConfig: ConnectorConfig) {
        logger.info { "Connector ${properties.id} is found. Current service type is ${connectorConfig.serviceType}" }
        if (connectorConfig.serviceType.isNullOrEmpty()) {
            if (properties.serviceType.isNullOrEmpty()) {
                throw IllegalArgumentException("Connector service type is not set. Please set the service type and restart.")
            }
            logger.info { "Connector ${properties.id} is not associated with a service type. Updating service type to ${properties.serviceType}" }
            service.updateConnectorServiceType(connectorConfig.id!!, properties.serviceType)
        }
        if (connectorConfig.serviceType != properties.serviceType) {
            throw IllegalArgumentException("Connector ${properties.id} is associated with service type ${connectorConfig.serviceType} but the service type is set to ${properties.serviceType}")
        }
    }

    companion object : KLogging()
}

fun main(args: Array<String>) {
    runApplication<ConnectorApplication>(*args)
}
