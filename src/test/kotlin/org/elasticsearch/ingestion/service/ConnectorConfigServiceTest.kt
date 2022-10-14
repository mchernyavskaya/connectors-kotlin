package org.elasticsearch.ingestion.service

import com.googlecode.catchexception.CatchException.catchException
import com.googlecode.catchexception.CatchException.caughtException
import io.mockk.mockk
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.elasticsearch.ingestion.connectors.base.ConfigurableField
import org.elasticsearch.ingestion.data.ConnectorConfig
import org.elasticsearch.ingestion.data.ConnectorRepository
import org.elasticsearch.ingestion.data.ConnectorStatus
import org.elasticsearch.ingestion.data.SyncStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ConnectorConfigServiceTest {
    private val repo = mock(ConnectorRepository::class.java)
    private val service = ConnectorConfigService(repo)
    private val connectorId = "someId"
    private val initialConnectorConfig = ConnectorConfig(
        id = connectorId,
        status = ConnectorStatus.created,
        name = "mockConnector",
        indexName = "mockConnectorIndex"
    )
    private val mockConnectorConfig = mockk<ConnectorConfig>()

    @BeforeEach
    internal fun setUp() {
        `when`(repo.findById(connectorId)).thenReturn(Optional.of(initialConnectorConfig))
        `when`(repo.save(any())).thenReturn(mockConnectorConfig)
    }

    @Test
    fun `we can get connector configuration when it exists`() {
        service.connectorConfiguration(connectorId)
        verify(repo).findById(connectorId)
    }

    @Test
    fun `we get empty value when connector doesn't exist`() {
        `when`(repo.findById(connectorId)).thenReturn(Optional.ofNullable(null))
        val found = service.connectorConfiguration(connectorId)
        verify(repo).findById(connectorId)
        assertNull(found)
    }

    @Test
    fun `when status is good, error is cleared`() {
        val status = ConnectorStatus.connected
        service.updateConnectorStatus(connectorId, status)
        ArgumentCaptor.forClass(ConnectorConfig::class.java).apply {
            verify(repo).save(capture())
            assertEquals(status, value.status)
            assertNull(value.error)
        }
    }

    @Test
    fun `when status is error and message is not provided, exception is thrown`() {
        val status = ConnectorStatus.error
        catchException { service.updateConnectorStatus(connectorId, status) }
        assertEquals(IllegalArgumentException::class.java, caughtException<IllegalArgumentException>().javaClass)
    }

    @Test
    fun `when status is error and message is provided, error is updated`() {
        val status = ConnectorStatus.error
        val errorMessage = "ConnectorStatus.error"
        ArgumentCaptor.forClass(ConnectorConfig::class.java).apply {
            service.updateConnectorStatus(connectorId, status, errorMessage)
            verify(repo).save(capture())
            assertEquals(status, value.status)
            assertEquals(errorMessage, value.error)
        }
    }

    @Test
    fun `when looking for connector package, id is passed`() {
        service.findConnectorConfig(connectorId)
        verify(repo).findById(eq(connectorId))
    }

    @Test
    fun `when configuration has all defaults connector becomes configured`() {
        val configurableFields = listOf(
            ConfigurableField("label1", "name1", "value1"),
            ConfigurableField("label2", "name2", "value2")
        )
        ArgumentCaptor.forClass(ConnectorConfig::class.java).apply {
            service.updateConnectorConfiguration(connectorId, configurableFields)
            verify(repo).save(capture())
            value.configuration.size shouldBeEqualTo 2
            value.configuration.forEach {
                val cf = configurableFields.find { field -> field.name == it.key }
                it.value.value shouldBeEqualTo cf?.defaultValue
                it.value.label shouldBeEqualTo cf?.label
            }
            value.status shouldBeEqualTo ConnectorStatus.configured
        }
    }

    @Test
    fun `when configuration has not all defaults connector needs configuration`() {
        val configurableFields = listOf(
            ConfigurableField("label1", "name1", "value1"),
            ConfigurableField("label2", "name2", null)
        )
        ArgumentCaptor.forClass(ConnectorConfig::class.java).apply {
            service.updateConnectorConfiguration(connectorId, configurableFields)
            verify(repo).save(capture())
            value.configuration.size shouldBeEqualTo 2
            value.configuration.forEach {
                val cf = configurableFields.find { field -> field.name == it.key }
                if (cf?.defaultValue != null) {
                    it.value.value shouldBeEqualTo cf.defaultValue
                } else {
                    it.value.value.shouldBeNull()
                }
                it.value.label shouldBeEqualTo cf?.label
            }
            value.status shouldBeEqualTo ConnectorStatus.needs_configuration
        }
    }

    @Test
    fun `when sync marked started the connector status is updated`() {
        ArgumentCaptor.forClass(ConnectorConfig::class.java).apply {
            service.markConnectorSyncStarted(connectorId)
            verify(repo).save(capture())
            value.lastSyncStatus shouldBeEqualTo SyncStatus.in_progress
            value.lastSynced.shouldNotBeNull()
            value.syncNow.shouldBeFalse()
        }
    }

    @Test
    fun `when sync marked successful the connector has proper status`() {
        ArgumentCaptor.forClass(ConnectorConfig::class.java).apply {
            service.markConnectorSyncCompleted(connectorId, SyncStatus.completed)
            verify(repo).save(capture())
            value.lastSyncStatus shouldBeEqualTo SyncStatus.completed
            value.lastSyncError.shouldBeNull()
            value.lastIndexedDocumentCount shouldBeEqualTo 0
            value.lastDeletedDocumentCount shouldBeEqualTo 0
            value.status shouldBeEqualTo ConnectorStatus.connected
            value.error.shouldBeNull()
        }
    }

    @Test
    fun `when sync marked successful with counts they are passed`() {
        ArgumentCaptor.forClass(ConnectorConfig::class.java).apply {
            service.markConnectorSyncCompleted(connectorId, SyncStatus.completed, 10, 5)
            verify(repo).save(capture())
            value.lastIndexedDocumentCount shouldBeEqualTo 10
            value.lastDeletedDocumentCount shouldBeEqualTo 5
        }
    }

    @Test
    fun `when sync marked failed the connector has proper status and error`() {
        val error = "some error"
        ArgumentCaptor.forClass(ConnectorConfig::class.java).apply {
            service.markConnectorSyncCompleted(connectorId, SyncStatus.error, 0, 0, error)
            verify(repo).save(capture())
            value.lastSyncStatus shouldBeEqualTo SyncStatus.error
            value.lastSyncError shouldBeEqualTo error
            value.lastIndexedDocumentCount shouldBeEqualTo 0
            value.lastDeletedDocumentCount shouldBeEqualTo 0
            value.status shouldBeEqualTo ConnectorStatus.error
            value.error shouldBeEqualTo error
        }
    }

    @Test
    fun `when service type is updated it is passed on`() {
        val serviceType = "some service type"
        ArgumentCaptor.forClass(ConnectorConfig::class.java).apply {
            service.updateConnectorServiceType(connectorId, serviceType)
            verify(repo).save(capture())
            value.serviceType shouldBeEqualTo serviceType
        }
    }
}
