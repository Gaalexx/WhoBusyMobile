# Инструкции для ИИ-агентов

Эти инструкции действуют для всего проекта WhoBusy: Android-приложения и Web-клиента на Kotlin Multiplatform.

## Карта проекта

- Перед изменениями прочитай [карту проекта](docs/AGENT_MAP.md) и относящийся к задаче код.
- При реализации каждой фичи, исправлении поведения или изменении архитектуры обновляй карту в рамках той же задачи.
- Отражай фактическое состояние: точки входа, расположение кода, связи слоёв, маршруты, ограничения и способы проверки.
- При перемещении или удалении кода исправляй соответствующие пути и описания. Для изменений, не влияющих на карту, не добавляй формальные записи.
- Карта — навигатор по текущему коду, а не журнал работы. Не записывай в неё планы как реализованные возможности и не копируй исходники.
- В итоговом сообщении кратко отмечай обновление карты и реально выполненные проверки. Отличай компиляцию тестов от их запуска.

## Архитектура

- Общие слои `data`, `domain`, `di`, `presentation` находятся в `shared/src/commonMain/kotlin/com/project/whobusy/`. Здесь же размещены дизайн-система `core/ui` и общие типы/операции дат `time`.
- `androidApp` — Android-host: Application, Activity, настройки сборки и AppGraph. Выбор mock/remote выполняет общий CalendarModule через AppConfiguration, переданную из Android BuildConfig. Платформенные HTTP-клиент, защищённое хранилище и BackHandler находятся в `shared/src/androidMain`. Общий код не должен зависеть от Android API.
- `webApp` запускает общий интерфейс через `shared.App` и браузерный `WebAppGraph`. По умолчанию используется REST на `/api/` того же origin; demo включается только явно через `-PwhoBusy.web.useMockData=true`, без автоматического fallback при ошибке сети. На широком Web включён `desktopLayout` по `docs/design/WhoBusy-Desktop-Board.svg`, на узком — общая мобильная компоновка; Android остаётся мобильным. История браузера и обработка его Back пока не подключены.
- Настроены только Android, JavaScript и Wasm; iOS/desktop цели не объявлены. Не заявляй их поддержку по одной лишь переносимости common-кода.
- `domain` содержит модели и контракты без зависимостей от Android, Compose, Ktor и DI-фреймворков.
- `data` реализует контракты `domain`; сетевые DTO и источники данных размещаются здесь.
- `presentation` работает с контрактами `domain`; общие MetroX-регистрации, `CalendarModule` и `DemoAppGraph` находятся в `shared/src/commonMain`; Android `NetworkModule` и `SessionModule` — в `shared/src/androidMain`, Android-граф `di/AppGraph.kt` — в `androidApp`, браузерный `di/WebAppGraph.kt` и HTTP-фабрика — в `shared/src/webMain`.
- Используй установленный стек: Kotlin, Compose, Ktor Client, Metro DI с compiler plugin и MetroX ViewModel, Navigation 3. KSP и Dagger/Hilt не используются. Версии зависимостей задавай в `gradle/libs.versions.toml`.
- Обычные ViewModel регистрируй в MetroX через `@Inject`, `@ViewModelKey` и `@ContributesIntoMap(AppScope::class)`. Для `SavedStateHandle` используй assisted-фабрику, создающую handle из `CreationExtras`; область ViewModel определяет `ViewModelStoreOwner`, а не `@SingleIn`.
- Форматируй `.kt` и `.kts` штатным Reformat Code из Android Studio с проектным `KOTLIN_OFFICIAL` и `.editorconfig`. Не применяй Ktlint для переформатирования: его переносы отличаются от Studio.
- Для новых фич следуй структуре GameHub: `presentation/<feature>/ui`, `viewmodel`, отдельные файлы состояния и команд, `StateFlow` и единая точка `onEvent`. Отделяй подключённый к ViewModel Root от чистого UI.
- Данные между экранами передавай полями сериализуемых типов `ScreenTypes` (идентификаторы, ISO-даты и другие необходимые аргументы). Root вызывает callback, а `AppNavHost` создаёт маршрут. Не используй Channel или SharedFlow для навигации. Завершение асинхронной операции сохраняй в StateFlow до явного `ResultHandled` из Root; callbacks не помещай в маршруты.
- Разделяй код по небольшим файлам с одной ответственностью. Общие кнопки и другие повторяемые UI-компоненты размещай в общей дизайн-системе UI, не дублируй их внутри экранов.
- Для коллекций UI с большим или заранее неизвестным числом элементов используй LazyColumn/LazyRow/LazyVerticalGrid и устойчивые ключи элементов. Не создавай такой список через forEach внутри Column/Row или одного lazy-item. Ограничивай высоту вложенных вертикальных списков и списков в диалогах. Небольшие фиксированные наборы (вкладки, дни недели, варианты enum) можно оставлять обычной компоновкой.
- Дизайн-система находится в `shared/src/commonMain/kotlin/com/project/whobusy/core/ui/`: `components` и `theme`, без зависимости от моделей приложения. Ресурсы — в `shared/src/commonMain/composeResources/`, доступ через `com.project.whobusy.resources.Res`; ошибки UI представлены `StringResource?`. Источник оформления описан в `docs/DESIGN_REFERENCE.md`.
- Используй общие `kotlinx.datetime` и `kotlin.time` для дат. Пакет `time` содержит aliases и совместимые операции, Clock с внедряемым временем и форматирование текущих русских шаблонов; это не универсальный локализатор.
- Для иконок используй стандартные Material Icons через общий `UiIcon`. Не рисуй собственные SVG или vector paths. Если подходящего значка нет в Material, ищи готовый в интернете, предпочтительно в официальных каталогах; такие исключения должны быть редкими и иметь указанный источник.
- Для каждого нового визуального компонента и экрана добавляй Compose Preview с автономными данными, без DI и сети; показывай значимые состояния.
- API-контракт и принятые правила из FunProject описаны в [API_CONTRACT.md](docs/API_CONTRACT.md). Android и Web по умолчанию используют REST. Android demo включает `-PwhoBusy.useMockData=true`, Web demo — отдельное `-PwhoBusy.web.useMockData=true`; Auth/Calendar/Workspace переключаются согласованно. Web dev-server проксирует `/api` через HTTPS origin из `WHOBUSY_API_PROXY_TARGET` (по умолчанию `https://whobusy.ru`); production-хостинг должен обслуживать API на том же origin. Для отдельного Web API URL используй `-PwhoBusy.web.apiBaseUrl=https://…`, учитывая backend CORS/SameSite.
- В удалённом режиме переключай Auth/Calendar/Workspace согласованно. Access-токен держи только в памяти; на Android refresh — в зашифрованном DataStore вне backup, ключ AES-GCM — в Android Keystore. В браузере refresh остаётся HttpOnly cookie: Ktor JS/Wasm отправляет `credentials=include`, `BrowserRefreshCookies` не читает и не задаёт Cookie/Set-Cookie. Не переносить refresh в JavaScript/localStorage и не ослаблять HttpOnly/Secure/SameSite для обхода CORS. Browser logout должен дождаться успешного серверного удаления cookie, иначе сессия сохраняется с ошибкой для повтора. Не сохраняй пароли и ключи в preferences, не логируй токены. Сохраняй native-ротацию refresh до публикации новой сессии и завершай очистку хранилища до возврата на экран входа.
- Проверяй изменения подходящими Gradle-задачами из карты. Для изменений только документации достаточно проверки путей и соответствия коду.
