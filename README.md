# WhoBusy

Доска планов и занятости по пространствам на Kotlin Multiplatform, Compose Multiplatform, Ktor Client, Metro DI и Navigation 3.

Общий интерфейс реализует [28 экранов Figma](https://www.figma.com/design/dDatbSFzoHbGIn2PWUqZnx?node-id=14-90): вход и регистрацию, доску день/неделя/месяц, пространства, приглашения и роли, личные и пространственные планы, повторения и профиль. На широком Web применяется отдельная desktop-компоновка по [WhoBusy-Desktop-Board.svg](docs/design/WhoBusy-Desktop-Board.svg): боковая навигация и пространства, календарь с подробностями, участники и редактор плана. Дополнительно доступно управление аккаунтами глобальным администратором.

## Структура

- `shared/src/commonMain/kotlin/com/project/whobusy/` — модели и контракты `domain`, моки и REST-слой `data`, MetroX ViewModel, экраны, Navigation 3 и дизайн-система `core/ui`.
- `shared/src/commonMain/composeResources/` — общие строки, изображения и архив оформления; доступ через Compose Resources.
- `shared/src/androidMain/` — OkHttp, Android Keystore/DataStore, платформенная блокировка моков и BackHandler.
- `androidApp` — Android-host `com.project.whobusy`: Application, Activity, системные панели и AppGraph; BuildConfig передаётся общему CalendarModule через AppConfiguration для выбора REST/demo.
- `shared/src/webMain/` — браузерный Metro-граф и Ktor HTTP-фабрика; JS/Wasm-адаптеры включают отправку HttpOnly cookie через Fetch credentials.
- `webApp` — браузерная точка входа для JavaScript и Wasm, build-конфигурация и dev-server API proxy; по умолчанию использует REST.

Отдельного модуля `core:ui` и исходного демонстрационного Greeting больше нет. Android и Web используют общие состояния, модели и расчёты; desktop-компоновка включается только для широкого браузера, Android и узкий Web сохраняют мобильный UI. Нативные iOS и desktop цели пока не настроены.

Metro 1.4.5 подключён Kotlin compiler plugin без KSP и Dagger/Hilt. Обычные ViewModel регистрируются в общем графе; календарь и редактор получают `SavedStateHandle` через assisted-фабрики. Сохранены области ViewModel в пределах сессии и результатов операций в StateFlow до `ResultHandled`.

Даты используют `kotlinx.datetime` и `kotlin.time`. Пакет `time` даёт совместимые операции календаря, Clock и форматирование существующих русских шаблонов. Расчёты повторений сохраняют локальное время без привязки к часовому поясу.

В браузере access остаётся только в памяти, refresh хранится браузером в HttpOnly cookie; клиент не читает `Set-Cookie` и не задаёт `Cookie` вручную. По умолчанию API доступен через `/api/` того же origin, чтобы сохранить backend `SameSite=Strict` без third-party cookie. История браузера и его Back ещё не подключены. Подробности платформенных границ — в [карте проекта](docs/AGENT_MAP.md#границы-мультиплатформенности).

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

Обе Web-версии используют Skiko и требуют доступный WebGL2-контекст; переключение JavaScript/Wasm это требование не меняет. Программный/HTML fallback интерфейса не реализован.

Dev-server проксирует `/api` на `https://whobusy.ru`, сохраняя browser credentials и HttpOnly/Secure/SameSite cookie. Для другого backend origin:

```bash
WHOBUSY_API_PROXY_TARGET=https://example.com ./gradlew :webApp:jsBrowserDevelopmentRun
```

Это HTTPS origin без `/api`, credentials, query и fragment. Для локального dev-server разрешён HTTP только на loopback-хосте; Android и внешний API по-прежнему требуют HTTPS.

## Источники данных

Android по умолчанию использует реальный API: `whoBusy.useMockData=false`, `whoBusy.apiBaseUrl=https://whobusy.ru/api/`. Для демонстрационной сборки:

```bash
./gradlew :androidApp:assembleDebug -PwhoBusy.useMockData=true
```

Для своего сервера:

```bash
./gradlew :androidApp:assembleDebug -PwhoBusy.apiBaseUrl=https://example.com/api/
```

Эти свойства также можно задать в пользовательском `~/.gradle/gradle.properties`; они относятся к Android-графу. У Web отдельные свойства:

```bash
# По умолчанию REST через /api/ текущего origin:
./gradlew :webApp:jsBrowserDevelopmentRun
# Явный demo без сетевых запросов:
./gradlew :webApp:jsBrowserDevelopmentRun -PwhoBusy.web.useMockData=true
# Необязательный отдельный API URL:
./gradlew :webApp:jsBrowserDevelopmentRun -PwhoBusy.web.apiBaseUrl=https://example.com/api/
```

При ошибке REST приложение показывает ошибку/повтор и не подменяет серверные данные моками. Отдельный API origin требует согласованных credentialed CORS и cookie-политики backend; одного URL недостаточно. Штатный backend с `SameSite=Strict` рассчитан на same-site размещение.

В demo-режиме поля входа заполнены: `demo@whobusy.test` / `demo1234` (также логин `demo`). Глобальный администратор: `admin` / `admin123`. Остальные демоаккаунты имеют пароль `demo1234`. В удалённом режиме демологин не заполняется.

Демоучастник состоит в трёх пространствах с разными ролями и имеет приглашение в четвёртое. Даты планов вычисляются относительно текущего дня. Данные моков и сессия живут в памяти: Android сбрасывает их вместе с процессом, Web — при перезагрузке страницы. Новая регистрация открывает сессию с пустым списком пространств.

## API и ограничения

[API_CONTRACT.md](docs/API_CONTRACT.md) содержит контракт, сверенный с [FunProject, commit f828103](https://github.com/Gaalexx/FunProject/tree/f8281030dc268ddb94e93be290f6095858d8500d) от 17.10.2026, версия FastAPI — 2.0.0.

Общие `RemoteAuthRepository`, `RemoteWorkspaceRepository` и `RemoteCalendarRepository` реализуют REST-контракт. На Android `RemoteSessionManager` управляет обновлением и восстановлением сессии: access и профиль находятся в памяти, refresh хранится зашифрованным через Android Keystore/DataStore в каталоге без backup. Ротация фиксируется до публикации новой сессии; параллельные 401 объединяются в одно обновление, защищённый запрос повторяется один раз.

На Android обычный выход очищает локальную сессию до возврата ко входу; серверный отзыв выполняется в фоне и остаётся best effort. В браузере обычный выход ждёт успешного ответа сервера, поскольку JavaScript не может удалить HttpOnly cookie; при сетевой ошибке сессия сохраняется для повтора. Выход на всех устройствах требует подтверждения успешного ответа сервера на обеих платформах. Аварийное завершение между серверной ротацией и сохранением native refresh после истечения grace может потребовать нового входа.

Повторения поддерживают ежедневный режим, будни, недели, месяцы и годы, включая `INTERVAL`, `COUNT` и `UNTIL` для используемого подмножества RRULE. Изменения/удаление относятся ко всей серии. Исключения отдельных вхождений, постоянный локальный кеш планов и offline CRUD не реализованы. Форматирование дат пока рассчитано на русский интерфейс.

## Размещение Web

Собирай и публикуй всю browser distribution, а не один `.js`: ей нужны HTML, Compose Resources, шрифты и Wasm/Skiko-файлы.

```bash
./gradlew :webApp:jsBrowserDistribution :webApp:wasmJsBrowserDistribution
```

Результаты — `webApp/build/dist/{js,wasmJs}/productionExecutable/`. Выбери одну distribution и размести её целиком на HTTPS-хостинге. Для nginx сохрани `/api/` за same-origin reverse proxy, а статику обслуживай с SPA fallback. Пример фрагмента существующего HTTPS server block для FastAPI на `127.0.0.1:8000`:

```nginx
root /srv/whobusy-web;
location /api/ {
    proxy_pass http://127.0.0.1:8000/;
    proxy_set_header Host $host;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
}
location / {
    try_files $uri $uri/ /index.html;
}
```

Cookie path должен оставаться `/api/auth`; не удаляй HttpOnly/Secure/SameSite. CORS не заменяет маршрутизацию API и не делает localhost same-site с `whobusy.ru`. Проверка живого `GET /api/health` подтверждает доступность сервера, но не успешный вход или CRUD — результаты проверок отдельно в карте проекта.
