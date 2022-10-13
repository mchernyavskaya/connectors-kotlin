package org.elasticsearch.ingestion.service

class ConnectorException(message: String) : Exception(message) {
    constructor(message: String, cause: Throwable) : this(message) {
        initCause(cause)
    }
}

class HealthCheckException(message: String) : Exception(message) {
    constructor(message: String, cause: Throwable) : this(message) {
        initCause(cause)
    }
}

class SyncJobAlreadyRunningException(connectorId: String, jobId: String) :
    Exception("Sync job [$jobId] for connector [$connectorId] is already running!")
