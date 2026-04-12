package ru.javarush.domain;

import ru.javarush.config.IslandSimulationConfig;

import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Создаёт пустую сетку по настройкам и раскладывает стартовые популяции случайно по клеткам.
 * Лимиты {@code maxPerLocation} из конфига здесь не проверяются — это задача следующих инкрементов.
 */
public final class IslandBuilder {

    private final Random random;

    public IslandBuilder(Random random) {
        this.random = Objects.requireNonNull(random, "random");
    }

    public Island build(IslandSimulationConfig config) {
        Objects.requireNonNull(config, "config");
        var settings = config.island();
        Island island = new Island(settings.width(), settings.height());
        scatterInitialPopulations(config, island, settings.initialAnimals());
        return island;
    }

    private void scatterInitialPopulations(
            IslandSimulationConfig config,
            Island island,
            Map<String, Integer> initialAnimals) {
        if (initialAnimals == null || initialAnimals.isEmpty()) {
            return;
        }
        var factory = new OrganismFactory(config);
        int h = island.height();
        int w = island.width();
        for (var entry : initialAnimals.entrySet()) {
            String speciesId = entry.getKey();
            int count = entry.getValue();
            if (count <= 0) {
                continue;
            }
            for (int i = 0; i < count; i++) {
                int row = random.nextInt(h);
                int col = random.nextInt(w);
                island.cell(row, col).add(factory.create(speciesId));
            }
        }
    }
}
