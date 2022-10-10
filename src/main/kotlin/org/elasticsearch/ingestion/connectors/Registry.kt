package org.elasticsearch.ingestion.connectors

import org.elasticsearch.ingestion.connectors.base.BaseConnector
import org.springframework.beans.factory.BeanFactoryUtils
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
@ComponentScan("org.elasticsearch.ingestion.connectors")
class Registry : ApplicationContextAware {
    private val connectors = mutableMapOf<String, BaseConnector>()
    private var applicationContext: ApplicationContext? = null

    @EventListener(ApplicationReadyEvent::class)
    fun onStart() {
        BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext!!, BaseConnector::class.java).forEach {
            connectors[it.value.serviceType()] = it.value
        }
    }

    fun getConnector(serviceType: String): BaseConnector? {
        return connectors[serviceType]
    }

    fun getConnectors(): List<BaseConnector> = connectors.values.toList()

    fun isRegistered(serviceType: String): Boolean {
        return connectors.containsKey(serviceType)
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }
}
