package ru.javarush.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * Загрузка {@link IslandSimulationConfig} из YAML на classpath.
 */
public final class IslandConfigLoader {

    public static final String DEFAULT_CLASSPATH_RESOURCE = "config/island.yml";

    private final ObjectMapper yamlMapper;
    private final IslandConfigValidator validator;

    public IslandConfigLoader() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.validator = new IslandConfigValidator();
    }

    /**
     * Читает конфиг из classpath (например {@code config/island.yml}).
     */
    public IslandSimulationConfig loadFromClasspath(String classpathLocation) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try (InputStream in = cl.getResourceAsStream(classpathLocation)) {
            Objects.requireNonNull(in, "Resource not found on classpath: " + classpathLocation);
            IslandSimulationConfig cfg = yamlMapper.readValue(in, IslandSimulationConfig.class);
            validator.validate(cfg);
            return cfg;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read YAML: " + classpathLocation, e);
        }
    }

    public IslandSimulationConfig loadDefault() {
        return loadFromClasspath(DEFAULT_CLASSPATH_RESOURCE);
    }
}
