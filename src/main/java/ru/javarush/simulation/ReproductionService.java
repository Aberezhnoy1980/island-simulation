package ru.javarush.simulation;

import ru.javarush.config.AnimalSettings;
import ru.javarush.config.IslandSimulationConfig;
import ru.javarush.domain.Animal;
import ru.javarush.domain.Island;
import ru.javarush.domain.Location;
import ru.javarush.domain.Organism;
import ru.javarush.domain.OrganismFactory;
import ru.javarush.domain.OrganismKind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Размножение на клетке: не более одного потомка на вид за тик; нужны две сытные особи одного вида
 * ({@link Animal#foodConsumedThisTick()} &gt; 0), место по {@link AnimalSettings#maxPerLocation()} и удачный бросок
 * {@code reproductionChancePercent} (или дефолт {@value #DEFAULT_REPRODUCTION_CHANCE_PERCENT}).
 * Растения здесь не обрабатываются.
 */
public final class ReproductionService {

    public static final int DEFAULT_REPRODUCTION_CHANCE_PERCENT = 15;

    private static final double FED_EPS = 1e-9;

    public void reproduce(Island island, IslandSimulationConfig config, Random random) {
        Objects.requireNonNull(island, "island");
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(random, "random");

        OrganismFactory factory = new OrganismFactory(config);

        for (int row = 0; row < island.height(); row++) {
            for (int col = 0; col < island.width(); col++) {
                reproduceAtCell(island.cell(row, col), config, factory, random);
            }
        }
    }

    private void reproduceAtCell(
            Location cell,
            IslandSimulationConfig config,
            OrganismFactory factory,
            Random random) {
        Map<String, List<Animal>> bySpecies = new HashMap<>();
        for (Organism o : cell.residentsView()) {
            if (o instanceof Animal animal) {
                bySpecies.computeIfAbsent(animal.speciesId(), k -> new ArrayList<>()).add(animal);
            }
        }

        for (Map.Entry<String, List<Animal>> entry : bySpecies.entrySet()) {
            String speciesId = entry.getKey();
            List<Animal> group = entry.getValue();

            AnimalSettings settings = config.animals().get(speciesId);
            if (settings == null || OrganismKind.fromConfig(settings.type()) == OrganismKind.PLANT) {
                continue;
            }

            List<Animal> fedEnough = new ArrayList<>();
            for (Animal a : group) {
                if (a.foodConsumedThisTick() > FED_EPS) {
                    fedEnough.add(a);
                }
            }
            if (fedEnough.size() < 2) {
                continue;
            }

            if (cell.countOf(speciesId) >= settings.maxPerLocation()) {
                continue;
            }

            int chancePercent = settings.reproductionChancePercent() != null
                    ? settings.reproductionChancePercent()
                    : DEFAULT_REPRODUCTION_CHANCE_PERCENT;
            if (chancePercent <= 0) {
                continue;
            }
            if (random.nextInt(100) >= chancePercent) {
                continue;
            }

            cell.add(factory.create(speciesId));
        }
    }
}
