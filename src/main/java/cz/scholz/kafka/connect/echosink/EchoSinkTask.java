/*
 * Copyright 2018, Jakub Scholz
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package cz.scholz.kafka.connect.echosink;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiFunction;

import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import org.apache.kafka.connect.header.Header;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * EchoSinkTask log records using Logger
 */
public class EchoSinkTask extends SinkTask {
    private static final Logger LOG = LoggerFactory.getLogger(EchoSinkTask.class);
    private static final String TRACING_OPERATION = "echo-sink";

    private Level logLevel;
    private BiFunction<Object, Object, Void> logOnLevel;
    private Tracer tracer;
    private Tracer.SpanBuilder spanBuilder;

    @Override
    public String version() {
        return new EchoSinkConnector().version();
    }

    @Override
    public void start(Map<String, String> props) {
        tracer = GlobalTracer.get();

        try {
            logLevel = Level.valueOf(props.get(EchoSinkConnector.LEVEL_CONFIG));
        } catch (IllegalArgumentException|NullPointerException e)   {
            LOG.warn("Failed to decode log level {}. Default log level INFO will be used.", props.get(EchoSinkConnector.LEVEL_CONFIG));
            logLevel = Level.INFO;
        }

        switch (logLevel)   {
            case INFO:
                logOnLevel = (key, value) -> {
                    LOG.info("Received message with key '{}' and value '{}'", key, value);
                    return null;
                };
                break;
            case ERROR:
                logOnLevel = (key, value) -> {
                    LOG.error("Received message with key '{}' and value '{}'", key, value);
                    return null;
                };
                break;
            case WARN:
                logOnLevel = (key, value) -> {
                    LOG.warn("Received message with key '{}' and value '{}'", key, value);
                    return null;
                };
                break;
            case DEBUG:
                logOnLevel = (key, value) -> {
                    LOG.debug("Received message with key '{}' and value '{}'", key, value);
                    return null;
                };
                break;
            case TRACE:
                logOnLevel = (key, value) -> {
                    LOG.trace("Received message with key '{}' and value '{}'", key, value);
                    return null;
                };
                break;
        }
    }

    @Override
    public void put(Collection<SinkRecord> sinkRecords) {
        for (SinkRecord record : sinkRecords) {
            // Extract tracing information
            Tracer.SpanBuilder spanBuilder = tracer.buildSpan(TRACING_OPERATION);
            Map<String, String> headers = new HashMap<>();
            Iterator<Header> iter = record.headers().iterator();

            while (iter.hasNext())  {
                Header header = iter.next();
                headers.put(header.key(), header.value().toString());
            }

            try {
                SpanContext parentSpan = tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapAdapter(headers));
                if (parentSpan != null) {
                    spanBuilder.addReference(References.FOLLOWS_FROM, parentSpan);
                }
            } catch (IllegalArgumentException e) {
                // pass
            }

            Span span = spanBuilder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER).start();

            // Log message
            log(record.key(), record.value());

            // Finish span
            span.finish();
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
