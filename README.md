# connectors-kotlin aka konnectors

The home of Elastic Enterprise Connector Clients in Kotlin. 
This repository contains the framework for customizing Elastic Enterprise Search native connectors, 
or writing your own connectors for advanced use cases.

**The connectors will be operated by an administrative user from within Kibana.**

## Getting started

### Prerequisites

- Elasticsearch is running and you can connect to it
- `spring.elasticsearch` options are properly configured in `application.yml`

### Building a docker image

```sh
docker build -t konnectors:latest .
```

### Running the docker image

```sh
docker run -p 8088:8088 konnectors:latest
```

### Running the application locally

```sh
./gradlew bootRun
```
