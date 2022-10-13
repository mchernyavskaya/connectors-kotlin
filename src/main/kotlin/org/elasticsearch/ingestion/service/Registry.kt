package org.elasticsearch.ingestion.service

import org.elasticsearch.ingestion.connectors.base.Connector
import org.elasticsearch.ingestion.connectors.example.ExampleConnector
import kotlin.reflect.KClass

object Registry {
    private val connectorClasses = mutableMapOf<String, KClass<out Connector>>()

    init {
        connectorClasses["exampleKotlin"] = ExampleConnector::class
    }

    fun connectorClass(serviceName: String): KClass<out Connector> {
        return connectorClasses[serviceName]
            ?: throw IllegalArgumentException("No connector class found for service $serviceName")
    }
}
