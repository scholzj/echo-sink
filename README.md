# Echo Sink Kafka Connect plugin

**Echo Sink** is a plugin for Kafka Connect. 
It implements only a sink connector.
The Sink connector receives messages from selected topic(s) and logs them using the Kafka Connect logger.
The main purpose of this plugin is to test Kafka Connect installation.
I created it to help me during development of [Strimzi Kafka operator for Kubernetes and OpenShift](http://strimzi.io).

## Configuration options

* `level`
Defines the log level on which the received messages will be logged.

## Examples

### Using REST API

1. Copy the `echo-sink-1.2.0.jar` jar file to your Kafka Connect plugin directory
2. Create a connector instance Kafka Connect REST API:
    ```
    curl -X POST -H "Content-Type: application/json" --data '{ "name": "echo-sink-test", "config": { "connector.class": "EchoSink", "tasks.max": "3", "topics": "my-topic", "level": "INFO" } }' http://localhost:8083/connectors
    ```

### Using Strimzi

1. Deploy [Strimzi](https://strimzi.io)
2. Deploy Kafka Connect using the `KafkaConnect` CR:
    ```yaml
    apiVersion: v1
    kind: Secret
    metadata:
      name: kafkaconnectbuild-pull-secret
    type: kubernetes.io/dockerconfigjson
    data:
      .dockerconfigjson: XXXXXXXXXX
    
    ---
    apiVersion: kafka.strimzi.io/v1beta2
    kind: KafkaConnect
    metadata:
      name: my-connect
      annotations:
        strimzi.io/use-connector-resources: "true"
    spec:
      replicas: 1
      bootstrapServers: my-cluster-kafka-bootstrap:9092
      config:
        key.converter: org.apache.kafka.connect.storage.StringConverter
        value.converter: org.apache.kafka.connect.storage.StringConverter
        key.converter.schemas.enable: false
        value.converter.schemas.enable: false
      build:
        output:
          type: docker
          image: my-registry/my-org/kafka-connect-build:latest
          pushSecret: kafkaconnectbuild-pull-secret
        plugins:
          - name: echo-sink-connector
            artifacts:
              - type: jar
                url: https://github.com/scholzj/echo-sink/releases/download/1.2.0/echo-sink-1.2.0.jar
                sha512sum: 2fd59f9c18d39de60b5582ae21144538ee6dc3b23127c200d8e3c15629bf3859e157a909a2ecc879633e6038510c3be74ca2907e1e5d07ddf5443cc412551878
    ```
3. Deploy the connector using the `KafkaConnector` CR:
    ```yaml
    apiVersion: kafka.strimzi.io/v1beta2
    kind: KafkaConnector
    metadata:
      name: echo-sink-connector
      labels:
        strimzi.io/cluster: my-connect
    spec:
      class: EchoSink
      tasksMax: 1
      config:
        level: "INFO"
        topics: "my-topic"
    ```