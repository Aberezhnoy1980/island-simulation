package ru.javarush.simulation;

/**
 * Фаза питания (растения / жертвы по матрице диеты).
 */
public final class FeedingPhase implements LifecyclePhase {

    private final FeedingService feedingService = new FeedingService();

    @Override
    public String id() {
        return "feeding";
    }

    @Override
    public void execute(SimulationContext context) {
        boolean parallelCellPasses = context.config().island().effectiveParallelMovementPlanning()
                || context.config().island().effectiveParallelPlantGrowthPlanning();
        feedingService.feedAll(context.island(), context.config(), context.random(), parallelCellPasses);
    }
}
