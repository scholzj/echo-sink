# Echo Sink Kafka Connect plugin

**Echo Sink** is a plugin for Kafka Connect. 
It implements only a sink connector.
The Sink connector receives messages from selected topic(s) and logs them using the Kafka Connect logger.
The main purpose of this plugin is to test a Kafka Connect installation.
I created it to help me during the development of [Strimzi](http://strimzi.io) Kafka operator for Kubernetes and OpenShift](http://strimzi.io).

## Configuration options

| Option                    | Description                                                                                                                                                                                                                                                                                                                    | Default |
|---------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|
| `level`                   | Defines the log level on which the received messages will be logged.                                                                                                                                                                                                                                                           | `INFO`  |
| `fail.task.after.records` | The tasks created by this connector will fail after receiving the specified number of records with an error. This is useful to test things such as status updated at task failures or automatic task restarts. If set to `0` or not set at all, this feature will be disabled and the connector will never fail intentionally. | `0`     |
| `fail.connector.startup`  | The connector will fail at startup. When set to true, the connector instance will never get running.                                                                                                                                                                                                                           | `false` |

## Examples

### Using REST API

1. Copy the `echo-sink-1.2.0.jar` jar file to your Kafka Connect plugin directory
2. Create a connector instance Kafka Connect REST API:
    ```
    curl -X POST -H "Content-Type: application/json" --data '{ "name": "echo-sink-test", "config": { "connector.class": "EchoSink", "tasks.max": "3", "topics": "my-topic", "level": "INFO" } }' http://localhost:8083/connectors
    ```

### Using with Strimzi

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
                url: https://github.com/scholzj/echo-sink/releases/download/1.4.0/echo-sink-1.4.0.jar
                sha512sum: 6d40b6334b9e6ff81a11176f23d082688ace58ff000570867dfabbd69a301c579c21f571985612c36cddd9d03454cea8cf0ace0d1f9335b5ef2e039f9781074a
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