package ru.javarush.view;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import ru.javarush.domain.Organism;
import ru.javarush.domain.OrganismKind;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Objects;

/**
 * Отображение {@code speciesId} → символ(ы) для консольной карты. Значения читаются из YAML;
 * для отсутствующих ключей используется запасной ASCII по {@link OrganismKind}.
 */
public final class SpeciesGlyphTable {

    public static final String DEFAULT_CLASSPATH_RESOURCE = "config/species-glyphs.yml";

    private final Map<String, String> bySpeciesId;

    private SpeciesGlyphTable(Map<String, String> bySpeciesId) {
        this.bySpeciesId = Map.copyOf(bySpeciesId);
    }

    /**
     * Таблица для тестов или кастомного набора глифов.
     */
    public static SpeciesGlyphTable fromMap(Map<String, String> speciesToGlyph) {
        Objects.requireNonNull(speciesToGlyph, "speciesToGlyph");
        return new SpeciesGlyphTable(speciesToGlyph);
    }

    public static SpeciesGlyphTable loadDefault() {
        return loadClasspath(DEFAULT_CLASSPATH_RESOURCE);
    }

    public static SpeciesGlyphTable loadClasspath(String classpathLocation) {
        Objects.requireNonNull(classpathLocation, "classpathLocation");
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try (InputStream in = cl.getResourceAsStream(classpathLocation)) {
            if (in == null) {
                return new SpeciesGlyphTable(Map.of());
            }
            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
            Map<String, String> raw = yamlMapper.readValue(in, new TypeReference<Map<String, String>>() {});
            return new SpeciesGlyphTable(raw);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read species glyphs: " + classpathLocation, e);
        }
    }

    public String glyphFor(Organism organism) {
        Objects.requireNonNull(organism, "organism");
        String g = bySpeciesId.get(organism.speciesId());
        if (g != null && !g.isBlank()) {
            return g.strip();
        }
        return asciiFallback(organism.kind());
    }

    private static String asciiFallback(OrganismKind kind) {
        return switch (kind) {
            case PREDATOR -> "P";
            case HERBIVORE -> "H";
            case PLANT -> "*";
        };
    }
}
