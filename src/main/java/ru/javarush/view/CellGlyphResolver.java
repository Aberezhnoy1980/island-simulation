package ru.javarush.view;

import ru.javarush.domain.Location;
import ru.javarush.domain.Organism;
import ru.javarush.domain.OrganismKind;

import java.util.List;
import java.util.Objects;

/**
 * Выбирает один «представительский» организм на клетке для отрисовки: приоритет хищник → травоядное → растение;
 * при равном приоритете — лексикографически меньший {@code speciesId} (детерминизм).
 */
public final class CellGlyphResolver {

    private CellGlyphResolver() {
    }

    public static String glyph(Location cell, SpeciesGlyphTable table) {
        Objects.requireNonNull(cell, "cell");
        Objects.requireNonNull(table, "table");
        Organism representative = representative(cell);
        if (representative == null) {
            return ".";
        }
        return table.glyphFor(representative);
    }

    static Organism representative(Location cell) {
        List<Organism> residents = cell.residentsView();
        if (residents.isEmpty()) {
            return null;
        }
        Organism best = null;
        for (Organism o : residents) {
            if (best == null) {
                best = o;
                continue;
            }
            int cmp = Integer.compare(kindRank(o.kind()), kindRank(best.kind()));
            if (cmp > 0) {
                best = o;
            } else if (cmp == 0 && o.speciesId().compareTo(best.speciesId()) < 0) {
                best = o;
            }
        }
        return best;
    }

    private static int kindRank(OrganismKind kind) {
        return switch (kind) {
            case PREDATOR -> 2;
            case HERBIVORE -> 1;
            case PLANT -> 0;
        };
    }
}
