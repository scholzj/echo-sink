/*
 * Copyright 2018, Jakub Scholz
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package cz.scholz.kafka.connect.echosink;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;

import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * EchoSinkTask log records using Logger
 */
public class EchoSinkTask extends SinkTask {
    private static final Logger log = LoggerFactory.getLogger(EchoSinkTask.class);

    private Level logLevel;
    private BiFunction<Object, Object, Void> logOnLevel;
    private String filename;

    @Override
    public String version() {
        return new EchoSinkConnector().version();
    }

    @Override
    public void start(Map<String, String> props) {
        try {
            logLevel = Level.valueOf(props.get(EchoSinkConnector.LEVEL_CONFIG));
        } catch (IllegalArgumentException|NullPointerException e)   {
            log.warn("Failed to decode log level {}. Default log level INFO will be used.", props.get(EchoSinkConnector.LEVEL_CONFIG));
            logLevel = Level.INFO;
        }

        switch (logLevel)   {
            case INFO:
                logOnLevel = (key, value) -> {
                    log.info("Received message with key '{}' and value '{}'", key, value);
                    return null;
                };
                break;
            case ERROR:
                logOnLevel = (key, value) -> {
                    log.error("Received message with key '{}' and value '{}'", key, value);
                    return null;
                };
                break;
            case WARN:
                logOnLevel = (key, value) -> {
                    log.warn("Received message with key '{}' and value '{}'", key, value);
                    return null;
                };
                break;
            case DEBUG:
                logOnLevel = (key, value) -> {
                    log.debug("Received message with key '{}' and value '{}'", key, value);
                    return null;
                };
                break;
            case TRACE:
                logOnLevel = (key, value) -> {
                    log.trace("Received message with key '{}' and value '{}'", key, value);
                    return null;
                };
                break;
        }
    }

    @Override
    public void put(Collection<SinkRecord> sinkRecords) {
        for (SinkRecord record : sinkRecords) {
            log(record.key(), record.value());
        }
    }

    @Override
    public void stop() {
        // Nothing to do
    }

    private void log(Object key, Object value)  {
        logOnLevel.apply(key, value);
    }
}
