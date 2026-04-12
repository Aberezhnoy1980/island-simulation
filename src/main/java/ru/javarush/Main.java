package ru.javarush;

import ru.javarush.config.IslandConfigLoader;
import ru.javarush.config.IslandSimulationConfig;
import ru.javarush.domain.Island;
import ru.javarush.domain.IslandBuilder;
import ru.javarush.domain.OrganismKind;
import ru.javarush.simulation.SimulationContext;
import ru.javarush.simulation.SimulationEngine;

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

        var random = new Random();
        var simulationContext = new SimulationContext(island, config, random);
        var engine = SimulationEngine.withDefaultPhases(simulationContext);
        int demoTicks = 3;
        for (int i = 0; i < demoTicks; i++) {
            engine.tick();
        }
        System.out.printf(
                "Demo simulation: %d ticks (movement, feeding, starvation death). Total creatures: %d%n",
                demoTicks,
                island.totalCreatures());
    }
}
