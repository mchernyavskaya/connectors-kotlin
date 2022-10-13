package org.elasticsearch.ingestion.service

import com.googlecode.catchexception.CatchException.catchException
import com.googlecode.catchexception.CatchException.caughtException
import io.mockk.mockk
import org.elasticsearch.ingestion.data.ConnectorConfig
import org.elasticsearch.ingestion.data.ConnectorRepository
import org.elasticsearch.ingestion.data.ConnectorStatus
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

}
