package ru.javarush.simulation;

/**
 * Фаза питания (растения / жертвы по матрице диеты). Логика будет добавлена позже.
 */
public final class FeedingPhase implements LifecyclePhase {

    @Override
    public String id() {
        return "feeding";
    }

    @Override
    public void execute(SimulationContext context) {
        // TODO: охота и поедание с учётом dietMatrix и веса
    }
}
