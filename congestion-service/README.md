# CongestionServiceApp

## Overview

Tracks the city-wide Congestion Level (0-8).

Part of the [TrafficFlow](../README.md) project. Independent Maven module, no
parent pom.

MQ: this service publishes to the ActiveMQ topic `congestion-topic` — see [`../common/`](../common). Broker URL and topic name come from the common `co.wethinkcode.trafficflow.mq.MqConfig` class alongside it in this module.

## Project structure

```
congestion-service/
├── pom.xml
└── src/main/java/co/wethinkcode/trafficflow/
    ├── CongestionServiceApp.java
    └── mq/
        └── MqConfig.java
```

## Build

```
mvn package
```

## Run

```
cd congestion-service
java -jar target/congestion-service.jar
```

Listens on port `7022`.

## Test

No automated tests yet. Manually verify it's up:

```
curl http://localhost:7022/health   # -> OK
curl http://localhost:7022/congestion  # -> This is how you get the current congestion level
curl -X POST http://localhost:7022/intersection -d (new_level) # -> This is how you update the current level to what you want
```

## Killing Ports

```
sudo lsof -i:7020  # -> checks the ports that are currently running
kill -9 {PID}  # -> kills the running port via the PID number
```

To add real tests, add JUnit 5 + the Surefire plugin to `pom.xml`, put tests under
`src/test/java/co/wethinkcode/trafficflow/`, and run `mvn test`.
