# connectors-kotlin aka konnectors

The home of Elastic Enterprise Connector Clients in Kotlin. This repository contains the framework for customizing
Elastic Enterprise Search native connectors, or writing your own connectors for advanced use cases.

**The connectors will be operated by an administrative user from within Kibana.**

## Getting started

There are two ways to run the application: with or without Docker. The Docker way is the easiest way to get started.
Running the application locally without Docker requires a few more steps.

### Prerequisites

- Elasticsearch is running and you can connect to it
- `spring.elasticsearch` options are properly configured in [application.yml](/src/main/resources/application.yml) or in
  a profile-specific `yml` file

### Docker-specific

- Docker is installed and running

### Non-Docker-specific

- Java 17 is installed. Pro tip: look at the [sdkman](https://sdkman.io/) project for managing multiple Java versions on
  your computer.

### Building and tagging a docker image

```sh
docker build -t konnectors:latest .
```

### Running the docker image locally

```sh
docker run -p 8088:8088 konnectors:latest
```

### Building the application locally

```sh
./gradlew clean build
```

### Running the tests

```sh
./gradlew test
```

### Running the application locally without docker

```sh
./gradlew bootRun
```
