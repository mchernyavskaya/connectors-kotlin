package org.elasticsearch.ingestion.service

import org.elasticsearch.ingestion.data.ConnectorJob
import org.elasticsearch.ingestion.data.ConnectorJobRepository
import org.elasticsearch.ingestion.data.SyncStatus
import org.springframework.stereotype.Service
import java.net.InetAddress
import java.util.*

@Service
class ConnectorJobService(
    private val jobRepository: ConnectorJobRepository,
    private val configService: ConnectorConfigService
) {
    fun findCurrentJob(connectorId: String): ConnectorJob? {
        return jobRepository.findByConnectorIdAndStatus(connectorId, SyncStatus.in_progress)
    }

    fun claimJob(connectorId: String): ConnectorJob? {
        val existingJob = findCurrentJob(connectorId)
        if (existingJob != null) {
            throw SyncJobAlreadyRunningException(connectorId, existingJob.id!!)
        }
        val connectorConfig = configService.markConnectorSyncStarted(connectorId)
        val job = ConnectorJob(
            connectorId = connectorId,
            status = SyncStatus.in_progress,
            workerHostname = InetAddress.getLocalHost().hostName,
            createdAt = Date(),
            connector = connectorConfig
        )
        return jobRepository.save(job)
    }

    fun completeJob(job: ConnectorJob, indexedCount: Long = 0, deletedCount: Long = 0, errorMessage: String? = null) {
        val status = if (errorMessage != null) SyncStatus.error else SyncStatus.completed
        val connectorConfig = configService.markConnectorSyncCompleted(
            job.connectorId!!,
            status,
            indexedCount,
            deletedCount,
            errorMessage
        )
        job.completedAt = Date()
        job.indexedDocumentCount = indexedCount
        job.deletedDocumentCount = deletedCount
        job.status = status
        job.error = errorMessage
        job.connector = connectorConfig
        jobRepository.save(job)
    }
}
