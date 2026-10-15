# WhoBusy

Мобильный календарь занятости: Kotlin, Jetpack Compose, Ktor Client, Dagger Hilt 2 с KSP и Navigation 3.

Реализованы вход, календарь день/неделя/месяц, выбор календарей коллег, профиль и выход, просмотр, создание, редактирование и удаление собственных событий. Есть состояния загрузки, ошибки и пустого календаря. Интерфейс основан на SVG из предоставленного FunProject-Mobile-Figma.zip.

## Запуск

По умолчанию используются моки, без API. Данные входа заполнены: `demo@whobusy.test` / `demo1234`. События создаются относительно текущей даты. Изменения и сессия хранятся в памяти и сбрасываются после завершения процесса.

- JDK 21 для Gradle daemon, JVM target 17.
- Android SDK Platform 37.1; путь к SDK в локальном `local.properties`.
- Gradle Wrapper 9.5.0, AGP 9.3.3; Kotlin встроен в AGP.
- Минимальный Android API 34, target SDK 36.

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest :app:lintDebug
./gradlew :app:connectedDebugAndroidTest
```

Последняя команда требует устройства/эмулятора. APK: `app/build/outputs/apk/debug/app-debug.apk`.

## Структура

- `app`: слои data, domain, di, presentation; UI-фичи разделены на ui, viewmodel, State и Command по образцу GameHub.
- `core:ui`: общие кнопки, поля, карточки, навигационные элементы, иконки, цвета и тема. Визуальные компоненты и чистые экраны имеют автономные Compose Preview.
- `WhoBusyApplication → MainActivity → WhoBusyApp → AppNavHost`: точка входа и Navigation 3. Смена пользователя пересоздаёт стек, после выхода нельзя вернуться к календарю кнопкой «Назад».

Версии: `gradle/libs.versions.toml`. Для ИИ-агентов: [инструкции](AGENTS.md), [карта проекта](docs/AGENT_MAP.md), [источник дизайна](docs/DESIGN_REFERENCE.md).

## Подключение API

```bash
./gradlew :app:assembleDebug \
  -PwhoBusy.useMockData=false \
  -PwhoBusy.apiBaseUrl=https://calendar.example.com/api/
```

Замените адрес на HTTPS-адрес своего FunProject. Реальный режим не заполняет демологин; UI использует те же доменные контракты. См. [API-контракт](docs/API_CONTRACT.md). Живой сервер не проверялся; сетевые контракты тестируются через Ktor MockEngine.

Пока не реализованы разворачивание/редактирование повторений RRULE, управление доступом к своему календарю и администрирование пользователей. Самостоятельной регистрации в API нет: аккаунты создаёт администратор.
