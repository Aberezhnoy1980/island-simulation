package ru.javarush;

import ru.javarush.config.IslandConfigLoader;
import ru.javarush.config.IslandSimulationConfig;
import ru.javarush.domain.Island;
import ru.javarush.domain.IslandBuilder;
import ru.javarush.domain.OrganismKind;

import java.util.Random;

/**
 * Точка входа: загрузка конфигурации, построение острова, краткая сводка.
 */
public final class Main {

    public static void main(String[] args) {
        IslandSimulationConfig config = new IslandConfigLoader().loadDefault();

        var settings = config.island();
        System.out.printf(
                "Island %d×%d, tick %d ms, stop: %s%n",
                settings.width(),
                settings.height(),
                settings.tickDurationMillis(),
                settings.stopCondition().type());

        Island island = new IslandBuilder(new Random()).build(config);

        System.out.printf("Cells: %d, total creatures on map: %d%n",
                settings.width() * settings.height(),
                island.totalCreatures());

        var totals = island.totalPopulationBySpecies();
        System.out.printf("Species tracked on island: %d (config defines %d kinds)%n",
                totals.size(),
                config.animals().size());

        long predatorsInConfig = config.animals().values().stream()
                .filter(s -> OrganismKind.PREDATOR.name().equalsIgnoreCase(s.type()))
                .count();
        System.out.printf("Predator species in config: %d; sample cell (0,0) population: %d%n",
                predatorsInConfig,
                island.cell(0, 0).totalCreatures());
    }
}
