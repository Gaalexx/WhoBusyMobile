# WhoBusy

Мобильная доска планов и занятости по пространствам: Kotlin, Jetpack Compose, Ktor Client, Hilt с KSP и Navigation 3.

Приложение реализует [обновлённые 28 экранов Figma](https://www.figma.com/design/dDatbSFzoHbGIn2PWUqZnx?node-id=14-90): вход и регистрацию, общую доску день/неделя/месяц, пространства и приглашения, участников и роли, личные планы с отдельной видимостью, планы пространства, повторения, профиль. Дополнительно доступно управление аккаунтами глобальным администратором.

## Запуск

По умолчанию используются моки, без сетевых запросов. Поля входа заполнены: `demo@whobusy.test` / `demo1234` (также логин `demo`). Глобальный администратор: `admin` / `admin123`. Остальные демоаккаунты имеют пароль `demo1234`.

Демоучастник состоит в трёх пространствах с разными ролями и имеет приглашение в четвёртое. Даты планов вычисляются относительно текущего дня. Данные и сессия хранятся в памяти и сбрасываются с процессом. Регистрация сразу открывает сессию с пустым списком пространств.

- JDK 21 для Gradle daemon, JVM target 17.
- Android SDK Platform 37.1; путь в локальном `local.properties`.
- Gradle Wrapper 9.5.0, AGP 9.3.3; Kotlin встроен в AGP.
- Минимальный Android API 34, target SDK 36.

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest :app:lintDebug
./gradlew :app:connectedDebugAndroidTest
```

Последняя команда требует устройства/эмулятора. APK: `app/build/outputs/apk/debug/app-debug.apk`.

## Структура

- `app`: слои data, domain, di, presentation; UI-фичи разделены на ui, viewmodel, State, Command и одноразовые эффекты.
- `core:ui`: общие компоненты, векторные иконки, цвета, тема и автономные Compose Preview.
- `WhoBusyApplication → MainActivity → WhoBusyApp → AppNavHost`: точка входа и типизированная Navigation 3. Три основные вкладки показывают плавающий бар поверх экрана, вложенные маршруты его скрывают.
- `MockAccountStore`, `MockWorkspaceRepository`, `MockCalendarRepository` используют общий набор аккаунтов, пространств, ролей и планов. Видимость и права проверяются в data до передачи UI.

Версии: `gradle/libs.versions.toml`. Для ИИ-агентов: [инструкции](AGENTS.md), [карта проекта](docs/AGENT_MAP.md), [источник дизайна](docs/DESIGN_REFERENCE.md).

## API

[API_CONTRACT.md](docs/API_CONTRACT.md) сверён с [FunProject](https://github.com/Gaalexx/FunProject/tree/1cfd95becbe11d098705543187bed8eeccdee17c) от 17.10.2026: регистрация, login/email, пространства, роли, приглашения, настройки доски и четыре уровня видимости планов.

Текущая реализация новых сценариев работает на моках. Исторический remote-слой относится к API до пространств; переключение `whoBusy.useMockData=false` не подключает новую механику. Перед реальной интеграцией нужны обновление DTO/маршрутов и реализация WorkspaceRepository для сети. Живой сервер в рамках этой задачи не вызывался.

Повторения поддерживают ежедневный режим, будни, недели, месяцы и годы, включая INTERVAL, COUNT и UNTIL для этого подмножества RRULE. Изменения/удаление относятся ко всей серии. Исключения отдельных вхождений и постоянное локальное хранилище не реализованы.
