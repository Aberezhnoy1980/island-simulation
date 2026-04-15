package ru.javarush.domain;

import ru.javarush.config.AnimalSettings;

/**
 * Растительность как отдельный вид участника клетки (в конфиге — запись {@code plant} с типом {@code PLANT}).
 */
public final class Plant extends Organism {

    public Plant(String speciesId, AnimalSettings settings) {
        super(speciesId, settings);
    }
}
