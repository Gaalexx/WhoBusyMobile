# WhoBusy

Доска планов и занятости по пространствам на Kotlin Multiplatform, Compose Multiplatform, Ktor Client, Metro DI и Navigation 3.

Общий интерфейс реализует [28 экранов Figma](https://www.figma.com/design/dDatbSFzoHbGIn2PWUqZnx?node-id=14-90): вход и регистрацию, доску день/неделя/месяц, пространства, приглашения и роли, личные и пространственные планы, повторения и профиль. Дополнительно доступно управление аккаунтами глобальным администратором.

## Структура

- `shared/src/commonMain/kotlin/com/project/whobusy/` — модели и контракты `domain`, моки и REST-слой `data`, MetroX ViewModel, экраны, Navigation 3 и дизайн-система `core/ui`.
- `shared/src/commonMain/composeResources/` — общие строки, изображения и архив оформления; доступ через Compose Resources.
- `shared/src/androidMain/` — OkHttp, Android Keystore/DataStore, платформенная блокировка моков и BackHandler.
- `androidApp` — Android-host `com.project.whobusy`: Application, Activity, системные панели и AppGraph; BuildConfig передаётся общему CalendarModule через AppConfiguration для выбора REST/demo.
- `webApp` — браузерная точка входа для JavaScript и Wasm, запускает общий интерфейс с `DemoAppGraph` и моками в памяти.

Отдельного модуля `core:ui` и исходного демонстрационного Greeting больше нет. Android и Web используют одни экраны, состояния, модели и расчёты. iOS и desktop цели пока не настроены.

Metro 1.4.5 подключён Kotlin compiler plugin без KSP и Dagger/Hilt. Обычные ViewModel регистрируются в общем графе; календарь и редактор получают `SavedStateHandle` через assisted-фабрики. Сохранены области ViewModel в пределах сессии и результатов операций в StateFlow до `ResultHandled`.

Даты используют `kotlinx.datetime` и `kotlin.time`. Пакет `time` даёт совместимые операции календаря, Clock и форматирование существующих русских шаблонов. Расчёты повторений сохраняют локальное время без привязки к часовому поясу.

Для REST в браузере ещё нужен auth/HTTP-адаптер: текущий native-клиент читает `Set-Cookie` и задаёт `Cookie` вручную. Браузер должен сам хранить HttpOnly refresh-cookie и отправлять её с credentials при подходящих CORS/SameSite. Web сейчас работает только с demo-данными; история браузера и его Back ещё не подключены. Подробности платформенных границ — в [карте проекта](docs/AGENT_MAP.md#границы-мультиплатформенности).

Версии зависимостей: `gradle/libs.versions.toml`. Для ИИ-агентов: [инструкции](AGENTS.md), [карта проекта](docs/AGENT_MAP.md), [источник дизайна](docs/DESIGN_REFERENCE.md).

## Сборка и проверки

Нужны JDK 21 для Gradle daemon и Android SDK Platform 37; путь к SDK задаётся в локальном `local.properties`. Используются Gradle Wrapper 9.5.1, AGP 9.1.1 и JVM target 17. Android: min API 34, target SDK 36.

```bash
./gradlew :androidApp:assembleDebug :androidApp:assembleRelease
./gradlew :shared:testAndroidHostTest :androidApp:testDebugUnitTest
./gradlew :shared:jsNodeTest
./gradlew :androidApp:lintDebug
./gradlew :shared:assembleAndroidDeviceTest :androidApp:assembleDebugAndroidTest
./gradlew :webApp:jsBrowserDevelopmentWebpack :webApp:wasmJsBrowserDevelopmentWebpack
```

`testAndroidHostTest` запускает общие и Android-host JVM-тесты, `jsNodeTest` — общие тесты JavaScript под Node.js; `testDebugUnitTest` — оставшиеся тесты Android-host. Задачи `assemble…Test` собирают инструментальные APK, но не запускают тесты на устройстве. Для запуска UI-сценариев нужен Android-девайс или эмулятор:

```bash
./gradlew :androidApp:connectedDebugAndroidTest -PwhoBusy.useMockData=true
```

Debug APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`. Release APK не подписан. Фактически выполненные проверки отражаются отдельно в [карте проекта](docs/AGENT_MAP.md#выполненные-проверки).

Запуск Web-приложения:

```bash
./gradlew :webApp:jsBrowserDevelopmentRun
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

## Источники данных

Android по умолчанию использует реальный API: `whoBusy.useMockData=false`, `whoBusy.apiBaseUrl=https://whobusy.ru/api/`. Для демонстрационной сборки:

```bash
./gradlew :androidApp:assembleDebug -PwhoBusy.useMockData=true
```

Для своего сервера:

```bash
./gradlew :androidApp:assembleDebug -PwhoBusy.apiBaseUrl=https://example.com/api/
```

Эти свойства также можно задать в пользовательском `~/.gradle/gradle.properties`; они относятся к Android-графу. Web использует общий demo-граф и не обращается к API.

В demo-режиме поля входа заполнены: `demo@whobusy.test` / `demo1234` (также логин `demo`). Глобальный администратор: `admin` / `admin123`. Остальные демоаккаунты имеют пароль `demo1234`. В удалённом режиме демологин не заполняется.

Демоучастник состоит в трёх пространствах с разными ролями и имеет приглашение в четвёртое. Даты планов вычисляются относительно текущего дня. Данные моков и сессия живут в памяти: Android сбрасывает их вместе с процессом, Web — при перезагрузке страницы. Новая регистрация открывает сессию с пустым списком пространств.

## API и ограничения

[API_CONTRACT.md](docs/API_CONTRACT.md) содержит контракт, сверенный с [FunProject, commit f828103](https://github.com/Gaalexx/FunProject/tree/f8281030dc268ddb94e93be290f6095858d8500d) от 17.10.2026, версия FastAPI — 2.0.0.

Общие `RemoteAuthRepository`, `RemoteWorkspaceRepository` и `RemoteCalendarRepository` реализуют REST-контракт. На Android `RemoteSessionManager` управляет обновлением и восстановлением сессии: access и профиль находятся в памяти, refresh хранится зашифрованным через Android Keystore/DataStore в каталоге без backup. Ротация фиксируется до публикации новой сессии; параллельные 401 объединяются в одно обновление, защищённый запрос повторяется один раз.

Обычный выход очищает локальную сессию до возврата ко входу; серверный отзыв выполняется в фоне и остаётся best effort. Выход на всех устройствах требует подтверждения успешного ответа сервера. Аварийное завершение между серверной ротацией и сохранением refresh после истечения grace может потребовать нового входа.

Повторения поддерживают ежедневный режим, будни, недели, месяцы и годы, включая `INTERVAL`, `COUNT` и `UNTIL` для используемого подмножества RRULE. Изменения/удаление относятся ко всей серии. Исключения отдельных вхождений, постоянный локальный кеш планов и offline CRUD не реализованы. Форматирование дат пока рассчитано на русский интерфейс.
