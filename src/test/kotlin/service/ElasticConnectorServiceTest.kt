package service

import com.googlecode.catchexception.CatchException.catchException
import com.googlecode.catchexception.CatchException.caughtException
import data.Connector
import data.ConnectorRepository
import data.ConnectorStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.stubbing.OngoingStubbing
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ElasticConnectorServiceTest {
    private val repo = mock(ConnectorRepository::class.java)
    private val service = ElasticConnectorService(repo)
    private val connectorId = "someId"
    private val mockConnector = Connector(
        id = connectorId,
        status = ConnectorStatus.created,
        name = "mockConnector",
        indexName = "mockConnectorIndex"
    )

    @BeforeEach
    internal fun setUp() {
        `when`(repo.findById(connectorId)).thenReturn(Optional.of(mockConnector))
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
        ArgumentCaptor.forClass(Connector::class.java).apply {
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
        ArgumentCaptor.forClass(Connector::class.java).apply {
            service.updateConnectorStatus(connectorId, status, errorMessage)
            verify(repo).save(capture())
            assertEquals(status, value.status)
            assertEquals(errorMessage, value.error)
        }
    }
}
