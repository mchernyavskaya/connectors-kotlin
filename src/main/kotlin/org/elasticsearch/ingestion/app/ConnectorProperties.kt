package org.elasticsearch.ingestion.app

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "connector", ignoreUnknownFields = true)
@ConstructorBinding
data class ConnectorProperties(
    val id: String? = null,
    val serviceType: String? = null
)
