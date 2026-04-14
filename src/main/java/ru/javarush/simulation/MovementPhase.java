package ru.javarush.simulation;

/**
 * Фаза перемещения животных между локациями.
 */
public final class MovementPhase implements LifecyclePhase {

    private final MovementService movementService = new MovementService();

    @Override
    public String id() {
        return "movement";
    }

    @Override
    public void execute(SimulationContext context) {
        boolean parallelPlanning = context.config().island().effectiveParallelMovementPlanning();
        movementService.relocateMobileOrganisms(context.island(), context.random(), parallelPlanning);
    }
}
