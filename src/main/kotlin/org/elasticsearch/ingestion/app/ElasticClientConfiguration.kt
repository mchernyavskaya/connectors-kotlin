package org.elasticsearch.ingestion.app

import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.ElasticsearchTransport
import co.elastic.clients.transport.rest_client.RestClientTransport
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.RestClients
import org.springframework.data.elasticsearch.client.elc.AutoCloseableElasticsearchClient
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration
import org.springframework.http.HttpHeaders


// this client is working with Elasticsearch bypassing the Spring Data repositories
@Configuration
class DirectClientConfiguration(val properties: ElasticClientProperties) {
    @Bean("elasticDirectClient")
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

// this client is for the Spring Data Elasticsearch repository
@Configuration
class RestClientConfiguration(val properties: ElasticClientProperties) : AbstractElasticsearchConfiguration() {
    @Bean("elasticRepositoryClient")
    override fun elasticsearchClient(): RestHighLevelClient {
        val compatibilityHeaders = HttpHeaders()
        compatibilityHeaders.add("Accept", "application/vnd.elasticsearch+json;compatible-with=7");
        compatibilityHeaders.add("Content-Type", "application/vnd.elasticsearch+json;compatible-with=7")
        val clientConfiguration = ClientConfiguration.builder()
            .connectedTo(properties.host)
            .withDefaultHeaders(compatibilityHeaders) // this variant for imperative code
        if (properties.username != null && properties.password != null) {
            clientConfiguration.withBasicAuth(properties.username!!, properties.password!!)
        }
        return RestClients.create(clientConfiguration.build()).rest()
    }
}
