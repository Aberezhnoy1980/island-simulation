package ru.javarush.simulation;

/**
 * Фаза гибели: голод (остальное — позже).
 */
public final class DeathPhase implements LifecyclePhase {

    private final DeathService deathService = new DeathService();

    @Override
    public String id() {
        return "death";
    }

    @Override
    public void execute(SimulationContext context) {
        boolean parallelCellPasses = context.config().island().effectiveParallelMovementPlanning()
                || context.config().island().effectiveParallelPlantGrowthPlanning();
        deathService.applyStarvation(context.island(), context.config().island(), parallelCellPasses);
    }
}

