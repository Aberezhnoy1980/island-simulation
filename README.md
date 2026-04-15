# Island Simulation

[![CI](https://github.com/Aberezhnoy1980/island-simulation/actions/workflows/ci.yml/badge.svg)](https://github.com/Aberezhnoy1980/island-simulation/actions/workflows/ci.yml)
[![Java 21](https://img.shields.io/badge/Java-21-437291?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)

Консольная симуляция острова на Java: сетка локаций, растительность, популяции животных, пошаговый жизненный цикл (перемещение, питание, размножение, гибель). Параметры мира и видов задаются конфигурацией, а не константами в коде.

**Статус:** рабочее ядро симуляции (фазы, питание, размножение, стоп-условия, валидация конфига), CLI (в т.ч. `--scheduled`, `--render-map-every`, `--seed`, `--ui=stream|live`), таблица глифов по видам (`config/species-glyphs.yml`), демо-конфиги `config/demo-island.yml` и `config/demo-predators.yml`, тесты на детерминизм и паритет режимов запуска; дальше — расширения (персистентность, доп. параллелизм по фазам).

## Зачем такой проект

Задача — собрать нетривиальную систему **без IoC-контейнера и Spring**: явные фабрики, композиция, ручная «сборка» графа объектов и конфигурация извне. Это не попытка написать «микро-Spring», а осознанная работа с теми же идеями, что используют и фреймворки (создание объектов, границы модулей, подмена реализаций), но средствами языка и дисциплины проектирования.

Когда граф зависимостей растёт, ручная сборка начинает «давить»: дублирование, легко забыть порядок инициализации, сложнее тестировать. Именно этот опыт и даёт интуитивное обоснование **IoC/DI** и контейнеров вроде Spring в промышленных проектах — не как «магию», а как ответ на реальную сложность связей. Параллельно отрабатываются ООП, коллекции, многопоточность и работа с конфигурацией на «голом» Java Core.

## Технологии

- Java 21  
- Maven  
- YAML: Jackson (`jackson-dataformat-yaml`)  
- Тесты: JUnit 5  
- Конфигурация по умолчанию: `src/main/resources/config/island.yml` (см. ТЗ); компактные демо-сцены: `config/demo-island.yml` (balanced) и `config/demo-predators.yml` (predator-heavy)

### Зависимости и плагины (зачем что)

| Компонент | Назначение |
|-----------|------------|
| **Jackson `jackson-dataformat-yaml`** | Чтение `island.yml` и `species-glyphs.yml` в объекты Java. Стандартный стек, меньше самописного парсинга и ошибок формата. |
| **JUnit 5** (`junit-jupiter`, scope `test`) | Модульные и интеграционные тесты без запуска приложения вручную. |
| **maven-compiler-plugin** (по умолчанию от Maven) | Компиляция под Java 21 (`maven.compiler.source/target` в `pom.xml`). |
| **maven-surefire-plugin** | Запуск тестов при `mvn test` / `verify`. |
| **maven-shade-plugin** | Сборка одного исполняемого JAR с `Main-Class` в манифесте — удобно запускать `java -jar ...` без classpath из десятков jar. |
| **exec-maven-plugin** | `mvn exec:java` без предварительной упаковки JAR — быстрый цикл при разработке. |

Дополнительные библиотеки не подключались намеренно: проект остаётся читаемым для тех, кто только осваивает экосистему Java.

### Режим «как в задании»: ~500 ms на тик, карта и статистика каждый тик, до естественной остановки

- **Пауза между тиками** берётся из `island.tickDurationMillis` в YAML (в дефолтном `config/island.yml` это **500**). Не передавайте `--no-delay` и не ставьте `--tick-delay-ms=0`, если нужна именно полсекунды. Явно: `--tick-delay-ms=500`.
- **Статистика каждый тик:** `--report-every=1`.
- **Карта каждый тик:** `--render-map-every=1`.
- Симуляция **всегда** ограничена сверху числом `--ticks` (по умолчанию 500). Чтобы чаще дойти до **стоп-условия из YAML** (`stopCondition`, например все животные мертвы), задайте большой лимит, например `--ticks=2000000`. Иначе выполнение закончится по **лимиту тиков**, а не по «естественной» причине.
- **Условия остановки** задаются в конфиге (`ALL_ANIMALS_DEAD`, `NO_HERBIVORES`, `NO_PREDATORS`) или разово через `--stop=...`. Для `ALL_ANIMALS_DEAD` «пустой» мир означает **нет животных**; растения (`Plant`) на клетках могут остаться — это всё ещё успешное срабатывание условия.

Пример (полный остров 100×20 — очень много строк карты; для наглядной карты см. `config/demo-island.yml`):

```bash
mvn -q exec:java -Dexec.args="--config=config/island.yml --ticks=2000000 --report-every=1 --render-map-every=1 --tick-delay-ms=500"
```

Компактная сетка для демонстрации карты в терминале:

```bash
mvn -q exec:java -Dexec.args="--config=config/demo-island.yml --ticks=500000 --report-every=1 --render-map-every=1 --tick-delay-ms=500"
```

Альтернативный демо-профиль с повышенной долей хищников:

```bash
mvn -q exec:java -Dexec.args="--config=config/demo-predators.yml --ticks=500000 --report-every=1 --render-map-every=1 --tick-delay-ms=500"
```

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
mvn -q exec:java -Dexec.args="--ui=live --tick-delay-ms=500 --ticks=1000"
mvn -q exec:java -Dexec.args="--tick-delay-ms=0 --report-every=1"
mvn -q exec:java -Dexec.args="--config=config/island.yml --ticks=200"
mvn -q exec:java -Dexec.args="--config=config/demo-island.yml --ticks=15 --no-delay --report-every=1 --render-map-every=3 --seed=1"
mvn -q exec:java -Dexec.args="--config=config/demo-predators.yml --ticks=15 --no-delay --report-every=1 --render-map-every=3"
```

Демо-конфиг (`demo-island.yml`): остров **24×8**, несколько видов; пример выше печатает карту каждые 3 тика (UTF-8 терминал).

Справка по флагам: `--help` или `-h`.

Лимит тиков: по умолчанию `500`, иначе первый аргумент-число или `--ticks=N`.  
Частота промежуточной статистики: `--report-every=N` (по умолчанию `50`).  
Пауза между тиками по умолчанию из `island.tickDurationMillis` в YAML; можно переопределить: `--tick-delay-ms=N` или `--no-delay` (эквивалентно нулю). В стартовой строке печатается **фактическая** пауза.  
`--scheduled` переключает запуск на `ScheduledExecutorService` (single-thread scheduled mode).  
`--ui=MODE` переключает режим вывода: `stream` (текущий построчный лог) или `live` (перерисовка одного экрана ANSI, карта+статистика на месте). В `live` экран обновляется каждый тик; `--report-every` и `--render-map-every` не влияют на частоту. Если интерактивная консоль недоступна, `live` автоматически переключается на `stream`.  
`--render-map-every=N` печатает карту острова каждые N тиков (`0` — отключено): один символ на клетку по «представителю» (хищник → травоядное → растение; при равенстве — лексикографически меньший `speciesId`). Глифы задаются в `config/species-glyphs.yml` (UTF-8); при отсутствии ключа для вида — запасной ASCII: `P` / `H` / `*`; пустая клетка — `.`.  
`--seed=N` делает прогон воспроизводимым: при одинаковом конфиге и аргументах результат будет повторяться.  
`--stop=TYPE` переопределяет stop condition из YAML для текущего запуска (`ALL_ANIMALS_DEAD`, `NO_HERBIVORES`, `NO_PREDATORS`).  
`--config`: путь к **файлу** на диске (если файл существует — читается он) или имя **classpath**-ресурса, например `config/island.yml` (по умолчанию при отсутствии флага).

Приложение строит остров из конфига и гоняет симуляцию до `stopCondition` в YAML (`ALL_ANIMALS_DEAD`, `NO_HERBIVORES`, `NO_PREDATORS`) или до лимита тиков: по умолчанию цикл в `SimulationRunner`, при `--scheduled` — `ScheduledSimulationRunner` (тот же движок фаз).

Порядок фаз: **`plantGrowth`** → `movement` → `feeding` → `reproduction` → `death`. Рост растений — `island.plantGrowthChancePercent` (в YAML; иначе дефолт 25), не выше `maxPerLocation` для вида `PLANT` на клетке.
Параллельное **планирование** (применение — по-прежнему последовательное): `island.parallelMovementPlanning` для `movement`, `island.parallelPlantGrowthPlanning` для `plantGrowth`. Если второй ключ не задан в YAML, рост растений наследует флаг движения (обратная совместимость со старыми конфигами).  
Дополнительно при включённом параллельном режиме безопасно распараллеливаются cell-local проходы без межклеточной мутации: сброс `foodConsumedThisTick` в `feeding` и starvation-pass в `death`.
В промежуточных отчётах по тикам дополнительно печатается `delta` (изменение числа существ за тик) и время фаз в миллисекундах.

Перед запуском выполняется валидация `island.yml`: размеры/тайминги, диапазоны процентов, ссылки на существующие виды в `initialAnimals` и `dietMatrix`, поддерживаемые `stopCondition`.

Консольная карта — `--render-map-every` и `SpeciesGlyphTable` / `species-glyphs.yml` (см. ТЗ).

## Known limitations

- При включенном parallel-планировании (`parallelMovementPlanning` / `parallelPlantGrowthPlanning`) часть вычислений использует `ThreadLocalRandom`; в этом режиме строгая воспроизводимость по `--seed` может отличаться от полностью sequential запуска.
- `--ui=live` требует интерактивный терминал (TTY с ANSI). В неинтерактивной среде (часть IDE-runner/CI) включается безопасный fallback в `stream`.

## CI

При push и pull request в ветки `main`, `develop` запускается [GitHub Actions](.github/workflows/ci.yml): `mvn -B verify` (тесты и сборка). После крупных рефакторингов локально при странных ошибках компиляции/загрузки классов имеет смысл `mvn clean verify`.

## Документация

- [Техническое задание и дорожная карта](docs/island-technical-spec.md)
- [Архитектура, паттерны, укомпонентовка](docs/architecture.md)

## Лицензия

[MIT](LICENSE)
