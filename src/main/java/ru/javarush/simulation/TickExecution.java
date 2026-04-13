package ru.javarush.simulation;

import java.util.Map;

/**
 * Результат одного тика: длительность фаз и изменение численности.
 */
public record TickExecution(
        long tickNumber,
        Map<String, Long> phaseDurationNanos,
        int creaturesBefore,
        int creaturesAfter
) {
}
