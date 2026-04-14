# Island Simulation

Консольная симуляция острова на Java: сетка локаций, растительность, популяции животных, пошаговый жизненный цикл (перемещение, питание, размножение, гибель). Параметры мира и видов задаются конфигурацией, а не константами в коде.

**Статус:** рабочее ядро симуляции (фазы, питание, размножение, стоп-условия, валидация конфига); дальше — расширения (UI, персистентность, доп. параллелизм по фазам).

## Зачем такой проект

Задача — собрать нетривиальную систему **без IoC-контейнера и Spring**: явные фабрики, композиция, ручная «сборка» графа объектов и конфигурация извне. Это не попытка написать «микро-Spring», а осознанная работа с теми же идеями, что используют и фреймворки (создание объектов, границы модулей, подмена реализаций), но средствами языка и дисциплины проектирования.

Когда граф зависимостей растёт, ручная сборка начинает «давить»: дублирование, легко забыть порядок инициализации, сложнее тестировать. Именно этот опыт и даёт интуитивное обоснование **IoC/DI** и контейнеров вроде Spring в промышленных проектах — не как «магию», а как ответ на реальную сложность связей. Параллельно отрабатываются ООП, коллекции, многопоточность и работа с конфигурацией на «голом» Java Core.

## Технологии

- Java 21  
- Maven  
- YAML: Jackson (`jackson-dataformat-yaml`)  
- Тесты: JUnit 5  
- Конфигурация по умолчанию: `src/main/resources/config/island.yml` (см. ТЗ)

## Сборка, тесты, запуск

```bash
mvn -q test
mvn -q package
java -jar target/Island-simulation-1.0-SNAPSHOT.jar
```

Либо без упаковки JAR:

```bash
mvn -q compile exec:java
mvn -q exec:java -Dexec.args="2000"
mvn -q exec:java -Dexec.args="--ticks=100"
mvn -q exec:java -Dexec.args="--ticks=1000 --report-every=100"
mvn -q exec:java -Dexec.args="--ticks=500 --no-delay"
mvn -q exec:java -Dexec.args="--ticks=500 --seed=42 --no-delay"
mvn -q exec:java -Dexec.args="--stop=NO_HERBIVORES --report-every=1"
mvn -q exec:java -Dexec.args="--scheduled --tick-delay-ms=10 --report-every=1"
mvn -q exec:java -Dexec.args="--render-map-every=25 --report-every=50"
mvn -q exec:java -Dexec.args="--tick-delay-ms=0 --report-every=1"
mvn -q exec:java -Dexec.args="--config=config/island.yml --ticks=200"
```

Справка по флагам: `--help` или `-h`.

Лимит тиков: по умолчанию `500`, иначе первый аргумент-число или `--ticks=N`.  
Частота промежуточной статистики: `--report-every=N` (по умолчанию `50`).  
Пауза между тиками по умолчанию из `island.tickDurationMillis` в YAML; можно переопределить: `--tick-delay-ms=N` или `--no-delay` (эквивалентно нулю). В стартовой строке печатается **фактическая** пауза.  
`--scheduled` переключает запуск на `ScheduledExecutorService` (single-thread scheduled mode).  
`--render-map-every=N` печатает карту острова (глифы клеток `P/H/*/.`) каждые N тиков (`0` — отключено).  
`--seed=N` делает прогон воспроизводимым: при одинаковом конфиге и аргументах результат будет повторяться.  
`--stop=TYPE` переопределяет stop condition из YAML для текущего запуска (`ALL_ANIMALS_DEAD`, `NO_HERBIVORES`, `NO_PREDATORS`).  
`--config`: путь к **файлу** на диске (если файл существует — читается он) или имя **classpath**-ресурса, например `config/island.yml` (по умолчанию при отсутствии флага).

Приложение строит остров из конфига и гоняет `SimulationRunner` до `stopCondition` в YAML (`ALL_ANIMALS_DEAD`, `NO_HERBIVORES`, `NO_PREDATORS`) или до лимита тиков.

Порядок фаз: **`plantGrowth`** → `movement` → `feeding` → `reproduction` → `death`. Рост растений — `island.plantGrowthChancePercent` (в YAML; иначе дефолт 25), не выше `maxPerLocation` для вида `PLANT` на клетке.
Параллельное **планирование** (применение — по-прежнему последовательное): `island.parallelMovementPlanning` для `movement`, `island.parallelPlantGrowthPlanning` для `plantGrowth`. Если второй ключ не задан в YAML, рост растений наследует флаг движения (обратная совместимость со старыми конфигами).
В промежуточных отчётах по тикам дополнительно печатается `delta` (изменение числа существ за тик) и время фаз в миллисекундах.

Перед запуском выполняется валидация `island.yml`: размеры/тайминги, диапазоны процентов, ссылки на существующие виды в `initialAnimals` и `dietMatrix`, поддерживаемые `stopCondition`.

Консольная псевдографика и Unicode — позже, см. ТЗ.

## CI

При push и pull request в ветки `main`, `develop` запускается [GitHub Actions](.github/workflows/ci.yml): `mvn -B verify` (тесты и сборка). После крупных рефакторингов локально при странных ошибках компиляции/загрузки классов имеет смысл `mvn clean verify`.

## Документация

- [Техническое задание и дорожная карта](docs/island-technical-spec.md)

## Лицензия

[MIT](LICENSE)
