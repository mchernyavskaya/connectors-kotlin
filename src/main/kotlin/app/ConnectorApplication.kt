package app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ConnectorApplication

fun main(args: Array<String>) {
    runApplication<ConnectorApplication>(*args)
}
