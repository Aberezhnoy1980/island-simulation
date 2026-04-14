package ru.javarush.simulation;

/**
 * В начале тика пополняется растительность на клетках (до лимита вида).
 */
public final class PlantGrowthPhase implements LifecyclePhase {

    private final PlantGrowthService plantGrowthService = new PlantGrowthService();

    @Override
    public String id() {
        return "plantGrowth";
    }

    @Override
    public void execute(SimulationContext context) {
        boolean parallelPlanning = context.config().island().effectiveParallelPlantGrowthPlanning();
        plantGrowthService.grow(context.island(), context.config(), context.random(), parallelPlanning);
    }
}
