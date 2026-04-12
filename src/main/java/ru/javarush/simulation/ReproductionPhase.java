package ru.javarush.simulation;

/**
 * Фаза размножения при выполнении условий. Логика будет добавлена позже.
 */
public final class ReproductionPhase implements LifecyclePhase {

    @Override
    public String id() {
        return "reproduction";
    }

    @Override
    public void execute(SimulationContext context) {
        // TODO: пары, лимиты maxPerLocation, вероятности
    }
}
