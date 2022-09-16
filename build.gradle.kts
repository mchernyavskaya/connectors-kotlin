import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    application
}

group = "org.elasticsearch"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // https://mvnrepository.com/artifact/org.springframework.data/spring-data-elasticsearch
    implementation("org.springframework.data:spring-data-elasticsearch:4.4.2")

    testImplementation(kotlin("test"))
    // https://mvnrepository.com/artifact/org.mockito/mockito-core
    testImplementation("org.mockito:mockito-core:4.8.0")
    // https://mvnrepository.com/artifact/eu.codearte.catch-exception/catch-exception
    testImplementation("eu.codearte.catch-exception:catch-exception:2.0")
    // https://mvnrepository.com/artifact/org.assertj/assertj-core
    testImplementation("org.assertj:assertj-core:3.23.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

application {
    mainClass.set("MainKt")
}
