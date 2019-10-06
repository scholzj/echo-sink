FROM strimzi/kafka:0.14.0-kafka-2.3.0
USER root:root
RUN mkdir -p /opt/kafka/plugins/echo-sink
COPY ./target/echo-sink-1.1.0.jar /opt/kafka/plugins/echo-sink/
USER 1001