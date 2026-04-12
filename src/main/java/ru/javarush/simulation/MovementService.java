package ru.javarush.simulation;

import ru.javarush.domain.Island;
import ru.javarush.domain.Location;
import ru.javarush.domain.Organism;
import ru.javarush.domain.OrganismKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Перемещение животных между клетками за один тик: до {@code speed} шагов, каждый шаг — случайное направление
 * (N/E/S/W); при выходе за границу шаг пропускается. Растения и организмы с {@code speed <= 0} не двигаются.
 */
public final class MovementService {

    private static final int[] DELTA_ROW = {-1, 0, 1, 0};
    private static final int[] DELTA_COL = {0, 1, 0, -1};

    public void relocateMobileOrganisms(Island island, Random random) {
        Objects.requireNonNull(island, "island");
        Objects.requireNonNull(random, "random");

        List<Relocation> plan = new ArrayList<>();
        int height = island.height();
        int width = island.width();

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Location here = island.cell(row, col);
                for (Organism organism : here.residentsView()) {
                    if (!isMobile(organism)) {
                        continue;
                    }
                    Location target = randomWalk(island, row, col, organism.settings().speed(), random);
                    if (target != here) {
                        plan.add(new Relocation(organism, here, target));
                    }
                }
            }
        }

        for (Relocation r : plan) {
            r.from.remove(r.organism);
            r.to.add(r.organism);
        }
    }

    private static boolean isMobile(Organism organism) {
        if (organism.kind() == OrganismKind.PLANT) {
            return false;
        }
        return organism.settings().speed() > 0;
    }

    private static Location randomWalk(Island island, int row, int column, int speed, Random random) {
        int r = row;
        int c = column;
        int h = island.height();
        int w = island.width();
        for (int step = 0; step < speed; step++) {
            int dir = random.nextInt(4);
            int nr = r + DELTA_ROW[dir];
            int nc = c + DELTA_COL[dir];
            if (nr >= 0 && nr < h && nc >= 0 && nc < w) {
                r = nr;
                c = nc;
            }
        }
        return island.cell(r, c);
    }

    private record Relocation(Organism organism, Location from, Location to) {
    }
}
