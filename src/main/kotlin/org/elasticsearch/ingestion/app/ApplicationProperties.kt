package org.elasticsearch.ingestion.app

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "connector", ignoreUnknownFields = true)
@ConstructorBinding
data class ConnectorProperties(
    val id: String? = null,
    val serviceType: String? = null
)

@ConfigurationProperties(prefix = "runner")
data class RunnerProperties(
    var pollingInterval: Long = 3000,
    var heartbeatInterval: Long = 180000,
    var terminationTimeout: Long = 60000
)

@ConfigurationProperties(prefix = "spring.elasticsearch")
data class ElasticClientProperties(
    var host: String = "localhost",
    var port: Int = 9200,
    var username: String? = null,
    var password: String? = null
)
