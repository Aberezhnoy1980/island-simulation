package ru.javarush.simulation;

/**
 * Одна фаза внутри тика (движение, питание и т. д.). Реализации сначала пустые — затем наполняются логикой.
 */
public interface LifecyclePhase {

    /** Стабильный идентификатор для логов и тестов (например {@code movement}). */
    String id();

    void execute(SimulationContext context);
}
