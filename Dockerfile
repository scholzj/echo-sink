FROM scratch

LABEL org.opencontainers.image.source=https://github.com/scholzj/echo-sink

COPY target/echo-sink-*.jar /
