package ru.javarush.config;

import ru.javarush.domain.OrganismKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Валидация структуры и диапазонов island.yml до запуска симуляции.
 */
public final class IslandConfigValidator {

    private static final Set<String> SUPPORTED_STOP_CONDITIONS = Set.of(
            "ALL_ANIMALS_DEAD",
            "NO_HERBIVORES",
            "NO_PREDATORS");

    public void validate(IslandSimulationConfig cfg) {
        List<String> errors = new ArrayList<>();
        if (cfg == null) {
            throw new IllegalArgumentException("Config is null");
        }

        validateIsland(cfg.island(), errors);
        validateAnimals(cfg.animals(), errors);
        validateInitialAnimals(cfg.island(), cfg.animals(), errors);
        validateDietMatrix(cfg.dietMatrix(), cfg.animals(), errors);

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Invalid island config:\n - " + String.join("\n - ", errors));
        }
    }

    private static void validateIsland(IslandSettings island, List<String> errors) {
        if (island == null) {
            errors.add("section 'island' is required");
            return;
        }
        if (island.width() <= 0) {
            errors.add("island.width must be > 0");
        }
        if (island.height() <= 0) {
            errors.add("island.height must be > 0");
        }
        if (island.tickDurationMillis() < 0) {
            errors.add("island.tickDurationMillis must be >= 0");
        }
        if (island.maxTicksWithoutFood() != null && island.maxTicksWithoutFood() <= 0) {
            errors.add("island.maxTicksWithoutFood must be > 0 when specified");
        }
        if (island.plantGrowthChancePercent() != null
                && !isPercent(island.plantGrowthChancePercent())) {
            errors.add("island.plantGrowthChancePercent must be in [0..100]");
        }
        if (island.stopCondition() != null
                && island.stopCondition().type() != null
                && !island.stopCondition().type().isBlank()
                && !SUPPORTED_STOP_CONDITIONS.contains(island.stopCondition().type().trim().toUpperCase())) {
            errors.add("island.stopCondition.type is not supported: " + island.stopCondition().type());
        }
    }

    private static void validateAnimals(Map<String, AnimalSettings> animals, List<String> errors) {
        if (animals == null || animals.isEmpty()) {
            errors.add("section 'animals' must contain at least one species");
            return;
        }
        for (Map.Entry<String, AnimalSettings> e : animals.entrySet()) {
            String species = e.getKey();
            AnimalSettings s = e.getValue();
            if (species == null || species.isBlank()) {
                errors.add("animals contains blank species id");
                continue;
            }
            if (s == null) {
                errors.add("animals." + species + " settings are missing");
                continue;
            }
            if (s.name() == null || s.name().isBlank()) {
                errors.add("animals." + species + ".name must not be blank");
            }
            if (s.weightKg() <= 0) {
                errors.add("animals." + species + ".weightKg must be > 0");
            }
            if (s.maxPerLocation() <= 0) {
                errors.add("animals." + species + ".maxPerLocation must be > 0");
            }
            if (s.speed() < 0) {
                errors.add("animals." + species + ".speed must be >= 0");
            }
            if (s.maxFoodKg() < 0) {
                errors.add("animals." + species + ".maxFoodKg must be >= 0");
            }
            if (s.reproductionChancePercent() != null && !isPercent(s.reproductionChancePercent())) {
                errors.add("animals." + species + ".reproductionChancePercent must be in [0..100]");
            }
            try {
                OrganismKind.fromConfig(s.type());
            } catch (RuntimeException ex) {
                errors.add("animals." + species + ".type is invalid: " + s.type());
            }
        }
    }

    private static void validateInitialAnimals(
            IslandSettings island,
            Map<String, AnimalSettings> animals,
            List<String> errors
    ) {
        if (island == null || island.initialAnimals() == null || animals == null) {
            return;
        }
        for (Map.Entry<String, Integer> e : island.initialAnimals().entrySet()) {
            String species = e.getKey();
            Integer count = e.getValue();
            if (!animals.containsKey(species)) {
                errors.add("island.initialAnimals contains unknown species: " + species);
                continue;
            }
            if (count == null || count < 0) {
                errors.add("island.initialAnimals." + species + " must be >= 0");
            }
        }
    }

    private static void validateDietMatrix(
            Map<String, Map<String, Integer>> dietMatrix,
            Map<String, AnimalSettings> animals,
            List<String> errors
    ) {
        if (dietMatrix == null || dietMatrix.isEmpty() || animals == null || animals.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Map<String, Integer>> hunterRow : dietMatrix.entrySet()) {
            String hunter = hunterRow.getKey();
            if (!animals.containsKey(hunter)) {
                errors.add("dietMatrix contains unknown hunter species: " + hunter);
                continue;
            }
            Map<String, Integer> preyRow = hunterRow.getValue();
            if (preyRow == null) {
                errors.add("dietMatrix." + hunter + " row is null");
                continue;
            }
            for (Map.Entry<String, Integer> preyCell : preyRow.entrySet()) {
                String prey = preyCell.getKey();
                Integer chance = preyCell.getValue();
                if (!animals.containsKey(prey)) {
                    errors.add("dietMatrix." + hunter + " contains unknown prey species: " + prey);
                }
                if (chance == null || !isPercent(chance)) {
                    errors.add("dietMatrix." + hunter + "." + prey + " must be in [0..100]");
                }
            }
        }
    }

    private static boolean isPercent(int v) {
        return v >= 0 && v <= 100;
    }
}
