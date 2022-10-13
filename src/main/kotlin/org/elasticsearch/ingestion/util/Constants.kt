package org.elasticsearch.ingestion.util

class Constants {
    companion object {
        const val CONNECTORS_INDEX = ".elastic-connectors"
        const val JOB_INDEX = ".elastic-connectors-sync-jobs"
        const val CONTENT_INDEX_PREFIX = "search-"
        const val CRAWLER_SERVICE_TYPE = "elastic-crawler"
    }
}
