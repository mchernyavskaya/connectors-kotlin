package org.elasticsearch.ingestion.app

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@Disabled("Disabled until we have a way to run elastic in a test")
class ConnectorApplicationTest {

    @Test
    fun contextLoads() {
    }

}
