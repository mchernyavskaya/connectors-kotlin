package org.elasticsearch.ingestion.app

import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.ElasticsearchTransport
import co.elastic.clients.transport.rest_client.RestClientTransport
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.elasticsearch.client.RestClient
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.elc.AutoCloseableElasticsearchClient

@ConfigurationProperties(prefix = "spring.elasticsearch")
data class ElasticClientProperties(
    var host: String = "localhost",
    var port: Int = 9200,
    var username: String? = null,
    var password: String? = null
)

@Configuration
class ElasticClientConfiguration(val properties: ElasticClientProperties) {
    @Bean
    fun elasticsearchClient(): AutoCloseableElasticsearchClient {
        val credentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(
            AuthScope.ANY,
            UsernamePasswordCredentials(properties.username, properties.password)
        )
        val restClient: RestClient = RestClient.builder(HttpHost(properties.host, properties.port))
            .setHttpClientConfigCallback { builder ->
                builder.setDefaultCredentialsProvider(credentialsProvider)
            }.build()
        val transport: ElasticsearchTransport = RestClientTransport(
            restClient, JacksonJsonpMapper()
        )
        // And create the API client
        return AutoCloseableElasticsearchClient(transport)
    }
}
