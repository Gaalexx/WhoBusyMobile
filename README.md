# WhoBusy

Доска планов и занятости по пространствам. Проект объединяет перенесённое Android-приложение на Kotlin, Jetpack Compose, Ktor Client, Hilt с KSP и Navigation 3 с заготовкой Kotlin Multiplatform для Android и Web.

Android-приложение реализует [обновлённые 28 экранов Figma](https://www.figma.com/design/dDatbSFzoHbGIn2PWUqZnx?node-id=14-90): вход и регистрацию, общую доску день/неделя/месяц, пространства и приглашения, участников и роли, личные планы с отдельной видимостью, планы пространства, повторения, профиль. Дополнительно доступно управление аккаунтами глобальным администратором.

## Структура

- `androidApp` — Android-приложение `com.project.whobusy`: экраны на Compose, внедрение зависимостей через Hilt, HTTP-клиент Ktor и Navigation 3.
- `core:ui` — Android-модуль дизайн-системы: общие UI-компоненты, тема и ресурсы оформления.
- `shared` — общий модуль исходного примера Compose Multiplatform.
- `webApp` — Web-приложение исходного примера с целями JavaScript и Wasm.

Android-экраны пока не перенесены в `shared/src/commonMain`; Web-приложение показывает исходный пример.

В `androidApp` находятся слои `data`, `domain`, `di`, `presentation`; UI-фичи разделены на `ui`, `viewmodel`, State и Command. Завершение операций хранится в StateFlow до `ResultHandled`. В `core:ui` находятся общие компоненты, стандартные Material Icons через `UiIcon`, цвета, тема и автономные Compose Preview.

`WhoBusyApplication → MainActivity → WhoBusyApp → AppNavHost` — точка входа и типизированная Navigation 3. Три основные вкладки показывают плавающий бар поверх экрана, вложенные маршруты его скрывают.

`MockAccountStore`, `MockWorkspaceRepository`, `MockCalendarRepository` используют общий набор аккаунтов, пространств, ролей и планов. Видимость и права проверяются в `data` до передачи UI.

`RemoteAuthRepository`, `RemoteWorkspaceRepository`, `RemoteCalendarRepository` используют REST API; `RemoteSessionManager` управляет обновлением и восстановлением сессии. Access-токен и профиль живут в памяти, refresh хранится зашифрованным через Android Keystore и DataStore в каталоге без backup.

Версии зависимостей: `gradle/libs.versions.toml`. Для ИИ-агентов: [инструкции](AGENTS.md), [карта проекта](docs/AGENT_MAP.md), [источник дизайна](docs/DESIGN_REFERENCE.md).

## Сборка и проверки

- JDK 21 для Gradle daemon, JVM target 17.
- Android SDK Platform 37; путь к SDK в локальном `local.properties`.
- Gradle Wrapper 9.5.1, AGP 9.1.1; Android-модули используют встроенную в AGP поддержку Kotlin.
- Минимальный Android API 34, target SDK 36.

```bash
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:testDebugUnitTest :core:ui:testDebugUnitTest
./gradlew :androidApp:lintDebug
./gradlew :androidApp:assembleDebugAndroidTest
```

`testDebugUnitTest` запускает JVM-тесты. `assembleDebugAndroidTest` собирает APK инструментальных тестов, но не запускает их на устройстве. Для запуска нужен Android-девайс или эмулятор:

```bash
./gradlew :androidApp:connectedDebugAndroidTest
```

Навигационные сценарии с демоданными запускайте со свойством `-PwhoBusy.useMockData=true`. Debug APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.

Запуск Web-приложения:

```bash
./gradlew :webApp:wasmJsBrowserDevelopmentRun
./gradlew :webApp:jsBrowserDevelopmentRun
```

## Данные Android-приложения

По умолчанию приложение использует реальный API: `whoBusy.useMockData=false`, `whoBusy.apiBaseUrl=https://whobusy.ru/api/`.

Для сборки с демонстрационными данными:

```bash
./gradlew :androidApp:assembleDebug -PwhoBusy.useMockData=true
```

Для переопределения адреса API укажите HTTPS URL:

```bash
./gradlew :androidApp:assembleDebug -PwhoBusy.apiBaseUrl=https://example.com/api/
```

Эти свойства также можно задать в локальном пользовательском `~/.gradle/gradle.properties`.

В mock-режиме поля входа заполнены: `demo@whobusy.test` / `demo1234` (также логин `demo`). Глобальный администратор: `admin` / `admin123`. Остальные демоаккаунты имеют пароль `demo1234`. В удалённом режиме демологин не заполняется.

Демоучастник состоит в трёх пространствах с разными ролями и имеет приглашение в четвёртое. Даты планов вычисляются относительно текущего дня. В mock-режиме сетевых запросов нет, данные и сессия хранятся в памяти и сбрасываются с процессом. Регистрация сразу открывает сессию с пустым списком пространств.

## API

[API_CONTRACT.md](docs/API_CONTRACT.md) содержит перенесённый контракт, сверенный с [FunProject, commit f828103](https://github.com/Gaalexx/FunProject/tree/f8281030dc268ddb94e93be290f6095858d8500d) от 17.10.2026, версия FastAPI — 2.0.0: регистрация, login/email, пространства, роли, приглашения, настройки доски и четыре уровня видимости планов.

По этому контракту access JWT действует 15 минут по умолчанию (`expires_in=900` в JSON). Refresh-token выдаётся только в `HttpOnly` cookie, ротируется при обновлении и имеет срок 30 дней. Предусмотрены `auth/refresh`, `auth/logout`, `auth/logout-all`; выход, смена/сброс пароля и блокировка отзывают refresh-сессии по правилам контракта. Native-клиент обрабатывает `Set-Cookie`, включая ротацию/удаление; refresh не передаётся полем JSON.

Пользовательский сервер и default base URL: [whobusy.ru](https://whobusy.ru/), `https://whobusy.ru/api/`. Свойство `whoBusy.useMockData` согласованно переключает все три репозитория — Auth, Workspace и Calendar. Учётные данные и токены через Gradle не передаются.

В исходниках реализованы регистрация/вход, зашифрованное сохранение refresh, восстановление при запуске, единое обновление access для параллельных 401 и однократный повтор запросов. До восстановления показывается загрузка; при временной ошибке доступны повтор и новый вход. Refresh передаётся вручную только выбранным auth-маршрутам этого API, а не через универсальный cookie jar.

Локальный выход очищает сохранённую сессию до возврата на экран входа; серверный отзыв выполняется в фоне и при отсутствии сети остаётся best effort. В профиле также есть «Выйти на всех устройствах» с подтверждением: повторный запрос и переходы блокируются на время выполнения, а при временной ошибке показывается сообщение и текущая сессия сохраняется.

Аварийное завершение между серверной ротацией и сохранением нового refresh может потребовать нового входа после истечения grace. Подробности и отличие logout от logout-all — в [контракте](docs/API_CONTRACT.md). Описание remote отражает код; результаты сборки, тестов и live-проверки фиксируются отдельно в [карте проекта](docs/AGENT_MAP.md).

Повторения поддерживают ежедневный режим, будни, недели, месяцы и годы, включая `INTERVAL`, `COUNT` и `UNTIL` для этого подмножества RRULE. Изменения/удаление относятся ко всей серии. Исключения отдельных вхождений и постоянный локальный кэш планов не реализованы; зашифрованное хранилище refresh существует отдельно.
