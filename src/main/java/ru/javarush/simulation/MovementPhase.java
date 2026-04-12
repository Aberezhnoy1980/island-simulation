package ru.javarush.simulation;

/**
 * Фаза перемещения животных между локациями. Логика будет добавлена позже.
 */
public final class MovementPhase implements LifecyclePhase {

    @Override
    public String id() {
        return "movement";
    }

    @Override
    public void execute(SimulationContext context) {
        // TODO: перемещение с учётом speed и границ острова; растения не двигаются
    }
}
