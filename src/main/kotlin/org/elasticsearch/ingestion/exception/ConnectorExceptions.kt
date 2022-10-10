package org.elasticsearch.ingestion.exception

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
