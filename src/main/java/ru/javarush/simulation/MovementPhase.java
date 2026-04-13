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
        boolean parallelPlanning = Boolean.TRUE.equals(context.config().island().parallelMovementPlanning());
        movementService.relocateMobileOrganisms(context.island(), context.random(), parallelPlanning);
    }
}
