package ru.javarush.domain;

import ru.javarush.config.AnimalSettings;

public final class Predator extends Animal {

    public Predator(String speciesId, AnimalSettings settings) {
        super(speciesId, settings);
    }
}
