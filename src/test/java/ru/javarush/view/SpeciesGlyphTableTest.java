package ru.javarush.view;

import org.junit.jupiter.api.Test;
import ru.javarush.config.IslandConfigLoader;
import ru.javarush.domain.OrganismFactory;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpeciesGlyphTableTest {

    @Test
    void defaultResourceMapsWolfToEmoji() {
        SpeciesGlyphTable table = SpeciesGlyphTable.loadDefault();
        var cfg = new IslandConfigLoader().loadDefault();
        var wolf = new OrganismFactory(cfg).create("wolf");
        assertEquals("🐺", table.glyphFor(wolf));
    }

    @Test
    void unknownSpeciesFallsBackToAsciiByKind() {
        SpeciesGlyphTable table = SpeciesGlyphTable.fromMap(Map.of());
        var cfg = new IslandConfigLoader().loadDefault();
        var wolf = new OrganismFactory(cfg).create("wolf");
        assertEquals("P", table.glyphFor(wolf));
    }
}
