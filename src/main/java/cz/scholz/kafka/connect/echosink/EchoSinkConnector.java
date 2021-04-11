/*
 * Copyright 2018, Jakub Scholz
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package cz.scholz.kafka.connect.echosink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;

public class EchoSinkConnector extends SinkConnector {

    public static final String LEVEL_CONFIG = "level";
    private static final ConfigDef CONFIG_DEF = new ConfigDef()
        .define(LEVEL_CONFIG, Type.STRING, "INFO", Importance.HIGH, "Log level on which the received records will be printed. If not specified, INFO will be used.");

    private String logLevel;

    @Override
    public String version() {
        return "1.2.0";
    }

    @Override
    public void start(Map<String, String> props) {
        AbstractConfig parsedConfig = new AbstractConfig(CONFIG_DEF, props);
        logLevel = parsedConfig.getString(LEVEL_CONFIG);
    }

    @Override
    public Class<? extends Task> taskClass() {
        return EchoSinkTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        ArrayList<Map<String, String>> configs = new ArrayList<>();

        Map<String, String> config = new HashMap<>(1);
        if (logLevel != null) {
            config.put(LEVEL_CONFIG, logLevel);
        }

        for (int i = 0; i < maxTasks; i++) {
            configs.add(config);
        }
        return configs;
    }

    @Override
    public void stop() {
        // Nothing to do
    }

    @Override
    public ConfigDef config() {
        return CONFIG_DEF;
    }
}
