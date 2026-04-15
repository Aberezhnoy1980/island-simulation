package ru.javarush.view;

import org.junit.jupiter.api.Test;
import ru.javarush.config.IslandConfigLoader;
import ru.javarush.config.IslandSimulationConfig;
import ru.javarush.domain.Location;
import ru.javarush.domain.OrganismFactory;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CellGlyphResolverTest {

    private final IslandSimulationConfig config = new IslandConfigLoader().loadDefault();
    private final OrganismFactory factory = new OrganismFactory(config);

    @Test
    void emptyCellIsDot() {
        Location cell = new Location(0, 0);
        SpeciesGlyphTable table = SpeciesGlyphTable.fromMap(Map.of());
        assertEquals(".", CellGlyphResolver.glyph(cell, table));
    }

    @Test
    void predatorBeatsHerbivore() {
        Location cell = new Location(0, 0);
        cell.add(factory.create("rabbit"));
        cell.add(factory.create("wolf"));
        SpeciesGlyphTable table = SpeciesGlyphTable.fromMap(Map.of("wolf", "W", "rabbit", "r"));
        assertEquals("W", CellGlyphResolver.glyph(cell, table));
    }

    @Test
    void herbivoreTieUsesLexicographicallySmallestSpeciesId() {
        Location cell = new Location(0, 0);
        cell.add(factory.create("rabbit"));
        cell.add(factory.create("deer"));
        SpeciesGlyphTable table = SpeciesGlyphTable.fromMap(Map.of("rabbit", "r", "deer", "d"));
        assertEquals("d", CellGlyphResolver.glyph(cell, table));
    }

    @Test
    void representativeEmptyIsNull() {
        assertNull(CellGlyphResolver.representative(new Location(0, 0)));
    }
}
