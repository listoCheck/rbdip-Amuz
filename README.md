# rbdip-bookstore (Java)

Учебный проект курса «Рефакторинг баз данных и приложений». Основной стек курса —
Java 21 + Spring Boot + Spring Data JPA (Hibernate) + PostgreSQL + Maven.

Это template-репозиторий: используйте кнопку **Use this template**, чтобы получить
свою копию для выполнения лабораторных работ.

## Навигация

- 📋 [Памятка студента](docs/STUDENT_GUIDE.md) — как начать, правила проверки, баллы
- 📝 [Задания по лабораторным работам](docs/ASSIGNMENTS.md) — что делать в каждой ЛР и какой гейт это проверяет
- ✅ [Actions](../../actions) — статус проверки вашего решения и баллы по гейтам (артефакт `score`)

## Как запустить локально

```bash
docker compose up -d          # поднимает PostgreSQL для ручного запуска приложения
mvn spring-boot:run           # приложение на http://localhost:8080
```

Тесты используют [Testcontainers](https://testcontainers.com/) и поднимают свою
изолированную БД в Docker — отдельно поднимать `docker compose` для тестов не нужно,
достаточно локально установленного и запущенного Docker:

```bash
mvn verify
```

## Структура проекта

```
src/main/java/com/rbdip/bookstore/
  product/   — товары
  order/     — заказы, позиции заказа, расчёт цены (OrderService, PricingCalculator)
  review/    — отзывы на товары
src/main/resources/db/migration/
  V1__init.sql — начальная схема (не редактировать), свои миграции — V2, V3, ...
src/test/java/com/rbdip/bookstore/reference/
  — эталонные тесты и правила (см. ниже)
scripts/write_score.py
  — собирает Checkstyle/PMD/JUnit/JaCoCo/PIT отчёты в score.json для автопроверки
```

## Что нельзя менять

Всё в пакете `src/test/java/com/rbdip/bookstore/reference/` (включая
`architecture/`) — это эталонные тесты и архитектурные правила. Они либо уже
проходят и должны оставаться зелёными после ваших изменений, либо изначально
красные и являются частью задания конкретной лабораторной — их прохождение и есть
цель, а не то, что можно скорректировать под свой код.

Список конкретных проблем, заложенных в проект, вы должны найти сами — это часть
ЛР1.

## Как оценивается работа

CI (`.github/workflows/ci.yml`) при каждом push прогоняет полный набор проверок и
формирует `score.json` с баллами по гейтам — подробности в тематическом плане курса.
Финальная сводная ведомость собирается автоматически отдельным сервисным
репозиторием, вручную ничего проверять не нужно.

## Completed laboratory branches

Each branch builds on the preceding one: `main -> lab1 -> lab2 -> lab3 -> lab4 -> lab5`.

| Branch | Work | Notes |
| --- | --- | --- |
| lab1 | Order responsibilities and pricing rules | [Lab 1](docs/LAB1.md) |
| lab2 | Characterization tests and normalized relations | [Lab 2](docs/LAB2.md) |
| lab3 | Customer-name expand/contract and HTTP load test | [Lab 3](docs/LAB3.md) |
| lab4 | Bounded order queries and independent reviews | [Lab 4](docs/LAB4.md) |
| lab5 | Full validation and JUnit 5 mutation testing | [Lab 5](docs/LAB5.md) |

Use JDK 21 and a running Docker daemon. On Windows, set `JAVA_HOME` to the JDK,
not the Java 17 JRE. The final branch configures the Docker API version needed
by this Testcontainers release for Docker 29.

```text
mvn clean verify
mvn org.pitest:pitest-maven:mutationCoverage
python scripts/write_score.py
```

Read the staged rollout procedure in `docs/LAB3.md` before updating an existing
production database. A fresh local/test database applies all migrations at startup.
