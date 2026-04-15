package ru.javarush.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Загрузка {@link IslandSimulationConfig} из YAML: файл на диске или ресурс classpath.
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
     * Сначала пробует обычный файл по пути {@code location}; если такого файла нет — classpath.
     */
    public IslandSimulationConfig load(String location) {
        Objects.requireNonNull(location, "location");
        if (location.isBlank()) {
            throw new IllegalArgumentException("location must not be blank");
        }
        Path path = Path.of(location).normalize();
        if (Files.isRegularFile(path)) {
            return loadFromFile(path);
        }
        return loadFromClasspath(location);
    }

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

    private IslandSimulationConfig loadFromFile(Path path) {
        try {
            IslandSimulationConfig cfg = yamlMapper.readValue(path.toFile(), IslandSimulationConfig.class);
            validator.validate(cfg);
            return cfg;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read YAML file: " + path.toAbsolutePath(), e);
        }
    }

    public IslandSimulationConfig loadDefault() {
        return loadFromClasspath(DEFAULT_CLASSPATH_RESOURCE);
    }
}
