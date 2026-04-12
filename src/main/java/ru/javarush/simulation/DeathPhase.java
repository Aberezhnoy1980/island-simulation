package ru.javarush.simulation;

/**
 * Фаза гибели (голод, возраст, съедены). Логика будет добавлена позже.
 */
public final class DeathPhase implements LifecyclePhase {

    @Override
    public String id() {
        return "death";
    }

    @Override
    public void execute(SimulationContext context) {
        // TODO: голод, естественная смерть, уже съеденные
    }
}
