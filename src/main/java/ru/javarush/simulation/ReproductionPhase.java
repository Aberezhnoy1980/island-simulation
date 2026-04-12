package ru.javarush.simulation;

/**
 * Фаза размножения при выполнении условий.
 */
public final class ReproductionPhase implements LifecyclePhase {

    private final ReproductionService reproductionService = new ReproductionService();

    @Override
    public String id() {
        return "reproduction";
    }

    @Override
    public void execute(SimulationContext context) {
        reproductionService.reproduce(context.island(), context.config(), context.random());
    }
}
