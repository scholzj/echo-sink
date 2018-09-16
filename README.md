# Echo Sink Kafka Connect plugin

**Echo Sink** is a plugin for Kafka Connect. 
It implements only a sink connector.
The Sink connector receives messages from selected topic(s) and logs them using the Kafka Connect logger.
The main purpose of this plugin is to test Kafka Connect installation.
I created it to help me during development of [Strimzi Kafka operator for Kubernetes and OpenShift](http://strimzi.io).

## Configuration options

* `level`
Defines the log level on which the recieved messages will be logged.

## Examples

1. Copy the `echo-sink-1.0.0.jar` jar file to your KAfka Connect plugin directory
2. Create a connector instance Kafka Connect REST API:
```
curl -X POST -H "Content-Type: application/json" --data '{ "name": "echo-sink-test", "config": { "connector.class": "EchoSink", "tasks.max": "3", "topics": "kafka-test-apps", "level": "INFO" } }' http://localhost:8083/connectors
```