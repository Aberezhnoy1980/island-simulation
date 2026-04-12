package ru.javarush.domain;

import ru.javarush.config.AnimalSettings;

/**
 * Животное (хищник или травоядный). Общее поведение появится в сервисах симуляции.
 */
public abstract class Animal extends Organism {

    protected Animal(String speciesId, AnimalSettings settings) {
        super(speciesId, settings);
    }
}
