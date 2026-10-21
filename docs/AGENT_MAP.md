# Карта WhoBusy для ИИ-агентов

## Состояние и правила

WhoBusy реализует пространства из Figma 02 (28 мобильных экранов), desktop Web по `docs/design/WhoBusy-Desktop-Board.svg` и FunProject API 2.0. Общие модели, data, ViewModel, UI, навигация и дизайн-система находятся в `shared`; `androidApp` и `webApp` запускают их на Android и в браузере. Отдельного модуля `core:ui` нет. Исходный пример Compose Multiplatform удалён.

На Android `BuildConfig.USE_MOCK_DATA` передаётся в общий `AppConfiguration`: по умолчанию `false` (REST), `-PwhoBusy.useMockData=true` включает моки согласованно для Auth/Calendar/Workspace. Штатный REST URL — `https://whobusy.ru/api/`, переопределяется `-PwhoBusy.apiBaseUrl=https://<server>/api/`. Web по умолчанию использует REST на `/api/` текущего origin через `WebAppGraph`; отдельное `-PwhoBusy.web.useMockData=true` явно включает demo. Ошибка сети не переключает приложение на моки.

Перед изменениями читай относящиеся к задаче файлы. Чистые экраны и Preview не используют DI, сеть и data. Карта описывает текущий код. Контракт: [API_CONTRACT.md](API_CONTRACT.md), источник оформления: [DESIGN_REFERENCE.md](DESIGN_REFERENCE.md).

## Модули и сборка

| Модуль | Назначение и связи |
| --- | --- |
| `shared` | KMP-библиотека для Android, JavaScript и Wasm: общий код в `commonMain`, адаптеры Android в `androidMain`, browser DI/HTTP/BackHandler в `webMain`, Fetch engine actual в `jsMain`/`wasmJsMain` |
| `androidApp` | Android-host `com.project.whobusy`, зависит от `shared`; Application, Activity, AppGraph, BuildConfig и ресурсы запуска |
| `webApp` | `webApp/src/webMain/kotlin/com/gaalex/whobusymp/main.kt` вызывает `shared.App` через ComposeViewport; JavaScript/Wasm, browser-граф и отдельные build-параметры, dev-server same-origin API proxy, экспорт production-пакета для FunProject |

Версии задаются в `gradle/libs.versions.toml`: Kotlin 2.4.20, AGP 9.1.1, Compose Multiplatform 1.12.1, Metro 1.4.5, kotlinx-datetime 0.8.0. Wrapper — Gradle 9.5.1, daemon — JDK 21. Android JVM target 17; SDK compile 37, target 36, min 34. Metro compiler plugin подключён в `shared` и `androidApp`, KSP и Dagger/Hilt не используются. iOS и desktop цели не объявлены.

## Точки входа и расположение

Общие пути относительно `shared/src/commonMain/kotlin/com/project/whobusy/`, если не указано иначе.

| Область | Путь | Ответственность |
| --- | --- | --- |
| Android-запуск | `androidApp/src/main/java/com/project/whobusy/{WhoBusyApplication,MainActivity}.kt` | Создание AppGraph с Android Context и AppConfiguration, фабрика MetroX, edge-to-edge, системные панели |
| Web-запуск | `shared/src/webMain/kotlin/com/project/whobusy/{App.kt,di/WebAppGraph.kt,di/WebAppConfiguration.kt}`, `webApp/src/webMain/kotlin/com/gaalex/whobusymp/main.kt` | Browser-граф, REST/demo-конфигурация, desktopLayout только для широкого Web |
| Приложение | `presentation/WhoBusyApp.kt`, `AppViewModel.kt`, `AppState.kt`, `AppCommand.kt`, `session/ui/SessionRestoreScreen.kt` | Тема, сессия, восстановление входа, повтор/новый вход; Android получает состояние сессии callback для системных панелей |
| Навигация | `presentation/navigation/` | ScreenTypes, NavDisplay, overlay-бар, область ViewModel сессии |
| Вход и регистрация | `presentation/{login,register}/{ui,viewmodel}/` | Логин/email, регистрация, ошибки, публикация сессии; LoginDefaults приходит из графа |
| Доска и её настройки | `presentation/calendar/{ui,viewmodel}/`, `presentation/board/{ui,viewmodel}/` | День/неделя/месяц, all-day, легенда, загрузка/мутации планов, фильтры пространств |
| Desktop Web | `presentation/desktop/ui/` | Навигационная rail, sidebar пространств/мини-календарь, календарь с боковыми подробностями, пространство/участники, расширенный редактор и профиль; общие ViewModel/команды |
| Календарные расчёты | `presentation/calendar/model/`, `time/` | Геометрия, календарные периоды, повторения, общие даты и Clock |
| Пространства | `presentation/spaces/{ui,viewmodel}/` | Список, создание/вступление, приглашения, роли, ключ, настройки, выход и удаление |
| План | `presentation/event/{ui,model,viewmodel}/` | Подробности/busy-only, общий черновик, видимость, даты/статус/повторы |
| Профиль и аккаунты | `presentation/profile/{ui,viewmodel}/`, `presentation/users/{ui,viewmodel}/` | Профиль, приглашения, logout/logout-all, пароль; CRUD аккаунтов для глобального admin |
| Domain | `domain/{model,repository}/` | Модели/контракты без Android, Compose, Ktor и DI; даты kotlinx.datetime |
| Моки | `data/mock/` | Единые аккаунты/пространства/планы, права и история отсоединения; DataLock имеет Android/JS/Wasm actual |
| Сессия | `data/session/`, `data/session/storage/{RefreshTokenStore,TokenCipher,BrowserRefreshTokenStore}.kt` | Access/user в памяти, native durable-ротация либо browser HttpOnly cookie; guard запрещает JS-хранение refresh |
| REST | `data/{network,remote,repository}/` | Общие API/DTO, Ktor-запросы, ошибки и реализации репозиториев; URL проверяется Ktor Url |
| Общий DI | `di/{AppConfiguration,CalendarModule,DemoAppGraph,AppViewModelFactory,ApplicationScope}.kt` | Конфигурация/выбор репозиториев, demo-граф, MetroX-фабрика, qualifier coroutine scope |
| Android DI и адаптеры | `shared/src/androidMain/kotlin/com/project/whobusy/` | NetworkModule/SessionModule/ApplicationContext, OkHttp, Keystore/DataStore, PlatformBackHandler |
| Android-граф | `androidApp/src/main/java/com/project/whobusy/di/AppGraph.kt` | Объединяет общие и Android-provider интерфейсы, принимает Context и AppConfiguration |
| Browser HTTP | `data/network/RefreshCookiePolicy.kt`, `shared/src/{webMain,jsMain,wasmJsMain}/kotlin/com/project/whobusy/data/network/` | Политика native/browser cookie, Ktor JS Fetch с credentials include, запретом redirect и no-store |
| Web proxy | `webApp/webpack.config.d/api-proxy.js` | `/api` development requests → HTTPS `WHOBUSY_API_PROXY_TARGET`, по умолчанию `https://whobusy.ru`; HttpOnly/Secure/SameSite не ослабляются |
| Web deployment | `webApp/build.gradle.kts`, `webApp/deployment/funproject/` | `exportFunProjectFrontend` собирает Wasm production distribution и папку `webApp/build/deploy/frontend/` для ручного копирования в FunProject, Git и штатного Docker Compose deploy |

`presentation/calendars/` и методы выбора ownerIds в CalendarRepository сохранены для совместимости старого кода, из навигации не вызываются. Исторический тип второй вкладки `ScreenTypes.Calendars` отображает SpacesRoot.

## Внедрение зависимостей

Android `WhoBusyApplication.appGraph` лениво создаёт `AppGraph` через Metro Factory, передавая application Context и AppConfiguration из BuildConfig. Общий CalendarModule выбирает mock/remote; Android NetworkModule создаёт JSON/HttpClient, SessionModule предоставляет защищённое хранилище и application CoroutineScope. `@SingleIn(AppScope::class)` ограничивает singleton временем жизни графа. Provider-функции `() -> Mock…` / `() -> Remote…` создают только выбранную ветку, поэтому demo не инициализирует HTTP или Keystore.

Web `shared/src/webMain/.../App.kt` создаёт `WebAppGraph` через Factory с `AppConfiguration`, HTTP-клиентом и CoroutineScope, использует общий CalendarModule для согласованного выбора трёх репозиториев и передаёт фабрику через LocalMetroViewModelFactory. Клиент/Scope освобождаются при выходе из композиции. WebAppConfiguration использует сгенерированный `WebBuildConfiguration` из `shared/build/generated/webConfiguration/` (задача `:shared:generateWebConfiguration`) и browser origin из JS/Wasm actual. Browser providers задают `BrowserRefreshCookies` и `BrowserRefreshTokenStore`. Последний store — fail-fast guard: HttpOnly refresh не читается, не записывается и не очищается из JavaScript. `DemoAppGraph` остаётся автономным общим mock-графом для smoke-тестов; обычная Web-точка входа не подменяет REST этим графом.

Общий AppViewModelFactory реализует MetroViewModelFactory с картами обычных провайдеров и assisted-фабрик. Девять обычных ViewModel используют `@Inject`, `@ViewModelKey`, `@ContributesIntoMap(AppScope::class)` и `metroViewModel()`. CalendarViewModel и EventEditorViewModel используют `@AssistedInject`; Factory создаёт SavedStateHandle из CreationExtras и зарегистрирована через `@ViewModelAssistedFactoryKey`. Их получает `assistedMetroViewModel()` с общим owner сессии. Объектами ViewModel управляет ViewModelStore, им не назначается singleton-scope.

## Границы мультиплатформенности

| Область | Реализованная граница | Оставшееся ограничение |
| --- | --- | --- |
| Даты | `time/`: aliases к kotlinx.datetime LocalDate/LocalDateTime/LocalTime/YearMonth, kotlin.time Instant/Duration; Clock и совместимые операции | Форматтер покрывает используемые русские шаблоны, универсальная локализация не реализована; повторения работают с локальным floating time |
| UI/ресурсы | Общие Compose UI/Material 3, ресурсы Res/StringResource, ViewModel/Navigation 3/Metro | Application/Activity, системные панели и значок запуска остаются Android-host; Compose Preview без сети/DI |
| Browser renderer | Skiko 0.150.1 использует WebGL2 в обеих Web-целях | Программный/HTML fallback интерфейса не реализован; Web требует доступный WebGL2 независимо от выбора JavaScript/Wasm |
| Сеть/refresh | Общие REST API/репозитории/SessionManager; Android OkHttp/secure storage; browser Ktor JS credentials include и HttpOnly cookie | NativeRefreshCookies вручную обрабатывает Cookie/Set-Cookie; BrowserRefreshCookies этого не делает. Cross-origin Web требует backend credentialed CORS и совместимого SameSite/Secure/Path; штатный путь — same-origin `/api` proxy |
| Секреты | Общие RefreshTokenStore/TokenCipher; Android Keystore/JCE и DataStore вне backup | Эти реализации Android-специфичны; для новых native-целей нужны защищённые платформенные реализации. Браузеру нельзя выдавать refresh в JS/localStorage |
| Выполнение | Моки используют kotlin.uuid/общие строковые операции и expect/actual DataLock; common coroutine-код переносим | JVM-lock используется только в androidMain; JS/Wasm actual опирается на однопоточное выполнение браузера |
| Back/история | PlatformBackHandler вызывает Android BackHandler; экранные callbacks общие | Web actual пока no-op; browser Back, History API и deep links не подключены |
| Дополнительные платформы | Настроены Android, JavaScript и Wasm | iOS/desktop цели и их адаптеры не объявлены, сборка на macOS/native не проверялась |

JS/Wasm подключают npm `@js-joda/timezone` 2.25.2 и инициализируют его в `shared/src/{jsMain,wasmJsMain}/kotlin/com/project/whobusy/time/TimeZoneInitialization.*.kt` по [инструкции kotlinx-datetime](https://github.com/Kotlin/kotlinx-datetime/tree/v0.8.0#note-about-time-zones-in-js). Корневой `build.gradle.kts` фиксирует `@js-joda/core` 5.7.0 через Yarn resolution для обеих целей: транзитивная версия 3.2.0 несовместима с новым timezone ([upstream issue](https://github.com/Kotlin/kotlinx-datetime/issues/627)). Версии задаются в каталоге, Yarn lock-файлы отслеживаются. Это добавляет региональные зоны и правила DST к базовой поддержке фиксированных offset; CalendarTimeTest проверяет Europe/Moscow и переход DST Europe/Berlin. Локальные повторения по-прежнему сохраняют floating time.

Браузер сам управляет HttpOnly cookie и ограничивает доступ JavaScript к [Set-Cookie](https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Set-Cookie) и [Cookie](https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Cookie). BrowserRefreshCookies возвращает только access из JSON; refresh не становится строкой JavaScript и не попадает в localStorage. JS/Wasm engine задаёт Fetch `credentials=include`, `redirect=error`, `cache=no-store`. `ApiConfiguration.allowLocalhostHttp` разрешает HTTP только для явно включённого Web loopback dev-server, не ослабляя Android HTTPS.

## Дизайн-система и строки

`shared/src/commonMain/kotlin/com/project/whobusy/core/ui/` содержит общие `theme/` и `components/`, без зависимости от моделей приложения.

- Тема задаёт палитру владельцев, светлые заливки карточек, Material 3 и типографику. Компоненты: кнопки/FAB, поля, Tabs/BottomBar/TopBar, DateCell, EventCard/Chip, Avatar, SwitchRow, Notice, ListRow, SectionTitle/SectionCard, ColorPalette, SelectionMark, RadioRow, Toggle, ChoicePill, Logo и часовая сетка. WhoBusySectionCard объединяет заголовок с Material-иконкой, разделитель и содержимое раздела.
- `components/WhoBusyIcon.kt` использует Material Icons через UiIcon: Filled и AutoMirrored ArrowBack, маркеры CheckCircle/RadioButtonUnchecked. Новые иконки выбираются из Material; редкие сторонние исключения требуют источника.
- Общие строки/изображения — `shared/src/commonMain/composeResources/`: `values/{strings,auth_strings,spaces_strings,board_strings,event_strings,admin_strings,ui_strings,desktop_strings}.xml`, `drawable/`, исходный архив Figma `files/design/`. Генерируется публичный `com.project.whobusy.resources.Res`. Состояния ошибок используют `StringResource?`, UI разрешает их через Compose Resources `stringResource`.
- Логотип внутри приложения — неизменённый пользовательский WhoBusy.png, `composeResources/drawable/whobusy_logo.png`, компонент WhoBusyLogo без tint на входе/восстановлении и сверху desktop rail вместо буквенной заглушки. Web HTML предварительно загружает тот же Compose-ресурс. WhoBusyCalendarBadge остаётся отдельной календарной иллюстрацией.
- `webApp/src/webMain/resources/` содержит неизменённые прежние Web PNG из FunProject: `logo.png`, `logo-192.png`, `favicon-32.png`, `apple-touch-icon.png`. `index.html` подключает favicon/Apple touch icon и показывает готовый логотип рядом с существующим индикатором до старта Compose. Статические PNG входят в JS/Wasm distribution и экспорт `frontend/static/`, отдельно копировать их в FunProject не нужно.
- Android launcher задан в `androidApp/src/main/AndroidManifest.xml` через `mipmap-anydpi/ic_launcher{,_round}.xml`; использует `androidApp/src/main/res/drawable-nodpi/whobusy_launcher_logo.png` и `drawable/ic_launcher_{foreground,background}.xml`. Launcher и backup-ресурсы не переносятся в Compose Resources.
- WhoBusyTextField имеет Plain/Filled; вход и регистрация используют Filled с голубой заливкой, синей рамкой 1 dp и красной ошибкой. Общие компоненты и чистые экраны имеют автономные Preview значимых состояний.
- WhoBusyScreenLayout объединяет статусный отступ, шапку и контент. AppBottomBar рисуется поверх NavDisplay: 80% ширины, высота 64 dp, радиус 25 dp, снизу 10 dp плюс navigation inset. Списки/FAB учитывают его перекрытие.

## Навигация и состояния

`AppViewModel` объединяет пользователя AuthRepository.currentUser и startup Loading/Ready/Failed в AppState. До завершения restoreSession навигация не показывается: SessionRestoreScreen отображает загрузку либо повтор/вход заново. Ошибка сети оставляет native encrypted refresh или browser HttpOnly cookie для повтора; невалидный/отозванный refresh приводит к гостевому входу. `AppNavHost` создаёт стек внутри `key(userId)`: Login для гостя, Calendar для сессии. Login → Registration; регистрация/вход меняют сессию и заменяют стек. Logout удаляет авторизованные маршруты и область ViewModel, в том числе при повторном входе того же пользователя.

## Авторизация и защищённое хранение

`AuthRepository.logout()` suspend и возвращает Result: на Android гость публикуется после очистки DataStore, в браузере — после успешного серверного logout с удалением HttpOnly cookie. Browser logout не выполняется best effort в фоне: при сетевой ошибке текущая сессия сохраняется, ProfileViewModel показывает ошибку для повтора. RemoteAuthRepository поддерживает login/register, restoreSession, logout и logoutAll. В ProfileScreen отдельная кнопка выхода на всех устройствах открывает подтверждение; ProfileViewModel блокирует повторный запрос, закрытие диалога и переходы во время выполнения. Ошибка logout-all оставляет текущую сессию и показывает сообщение. Моки не создают постоянную сессию и не обращаются к сети; оба вида выхода очищают только локальную моковую сессию.

`AuthAPI` получает access_token/token_type/expires_in из JSON; RefreshCookiePolicy определяет платформенную границу. NativeRefreshCookies извлекает refresh_token из Set-Cookie, BrowserRefreshCookies оставляет его браузеру. Access JWT и пользователь остаются только в SessionStore. На Android в EncryptedRefreshTokenStore записывается зашифрованный JSON с refresh и точным base URL; при смене сервера прежний токен не отправляется на другой адрес. Пароли, access и расшифровочный ключ в DataStore не хранятся.

`shared/src/androidMain/kotlin/com/project/whobusy/data/session/storage/AndroidKeystoreTokenCipher.kt` создаёт неэкспортируемый AES-256 ключ AndroidKeyStore; `AesGcmTokenCipher.kt` использует AES/GCM/NoPadding, случайный 96-битный IV, 128-битный authentication tag и AAD с назначением/версией. Preferences DataStore хранит только byte[] envelope magic/version/IV/ciphertext/tag в `Context.noBackupFilesDir/datastore/auth_session.preferences_pb`; файл не переносится backup/device transfer. Криптография выполняется на IO. Повреждение файла/шифротекста или потеря ключа сбрасывает сохранённое значение, без хранения plaintext и без генерации нового ключа при чтении старого ciphertext. Новый ключ создаётся только при записи.

`RemoteSessionManager` сериализует обновление через Mutex. AuthenticatedHttpClient запрашивает действующий access, при 401 обновляет refresh и повторяет запрос один раз; 403/сетевые ошибки не запускают повтор мутации. Версия обновления позволяет объединить параллельные 401 даже при одинаковом JWT; generation не даёт ответу старой сессии затронуть новую. Ротация, фиксация нового refresh и публикация access защищены NonCancellable-транзакцией от отмены вызывающего coroutine; Android-хранилище выполняет дисковые/криптографические операции на IO. После повторного 401/отозванного refresh локальная сессия очищается.

После каждого browser refresh читается auth/me, а не используется старый профиль: HttpOnly cookie общая для вкладок и могла сменить аккаунт. Если user ID изменился, новая согласованная сессия публикуется с новым generation, но вызвавшая обновление старая операция отменяется через CancellationException и не выполняется/не повторяется под другим пользователем. Все устаревшие browser leases также отменяются, а не возвращают AuthenticationException: обработчик ошибки старого экрана не должен вызвать logout уже новой сессии. Новый user ID сбрасывает навигацию через общий session key. Это защита от смены identity при refresh, не полная синхронизация вкладок.

Native-клиент вручную передаёт refresh только в auth/refresh, auth/logout и PATCH auth/me (смена пароля должна сохранить текущую семью сессии); access/refresh для PATCH берутся одной атомарной парой. Это ограниченный transport cookie value, не общий persistent CookieJar. В браузере эти запросы отправляют cookie через Fetch credentials, включая PATCH профиля; обычные бизнес-запросы используют Bearer. HTTPS обязателен, кроме явно разрешённого Web loopback; редиректы отключены. Android-выход сначала завершает локальную durable-очистку, серверный отзыв выполняется best effort в application scope. Browser logout и logout-all обеих платформ очищают локальную сессию после успешного ответа. Access-токены уже выпущенной сессии сервер допускает до exp, если аккаунт не заблокирован/удалён.

После смерти процесса новый access и пользователь загружаются через refresh. Аварийное завершение после серверной ротации, но до получения/записи нового refresh, остаётся ограничением протокола: сервер даёт 30 секунд grace старому токену, затем повтор старого токена отзывает семью. Отмена coroutine закрыта транзакцией клиента; атомарность с удалённым сервером не заявляется.

## Календарь и формы

### Desktop Web

`WhoBusyApp`/`AppNavHost` принимают `desktopLayout` с default false; Android не меняет мобильную компоновку. Web передаёт capability true, а `presentation/desktop/DesktopLayout.kt` включает desktop только при ширине от 1160 dp; ниже показываются общие мобильные экраны с bottom bar. Изменение ширины не сбрасывает back stack, черновик/область ViewModel или выбранный режим календаря. Состояние календаря, авторизация, маршруты и CRUD не дублируются отдельными browser-репозиториями. Источник — неизменённый пользовательский SVG `docs/design/WhoBusy-Desktop-Board.svg`, 139 685 байт.

- `presentation/desktop/ui/DesktopNavigationRail.kt` и `DesktopBoardSidebar.kt`: постоянная навигация, пространства и их фильтры, собственные планы, мини-календарь, создание/вступление; переходы через существующий AppNavHost. Rail сохраняет ширину 80 dp; подписи используют 72 dp, одну строку и нулевой межбуквенный интервал, чтобы «Пространства» не переносилось и не обрезалось при штатном масштабе текста. Состояния Доски/Пространств имеют автономные Preview.
- `DesktopCalendarScreen.kt`, `DesktopCalendarToolbar.kt`, `DesktopTimeCalendar.kt`, `DesktopMonthCalendar.kt`, `DesktopPlanDetailsScreen.kt`: день/неделя/месяц, today/перелистывание, временная сетка и карточки; в месяце справа agenda, выбранный план открывает боковые подробности. При уменьшении доступной ширины sidebar скрывается.
- `DesktopSpacesScreen.kt` и `DesktopSpaceNavigation.kt`: вкладка пространств открывает широкую панель с реальными карточками, приглашениями и состояниями загрузки/ошибки/пустого списка; пространство выбирается пользователем, автоматического перехода нет. Sidebar прокручивает список вместе с действиями и подсказкой, поэтому доступен в низком широком окне.
- `DesktopSpaceDetailsScreen.kt` и `DesktopSpaceInvitePanel.kt`: центральные участники/ожидающие приглашения, роль, приглашение по логину с выбором роли, копирование/обновление ключа. Участник открывает общий маршрут роли, настройки — существующий экран; права берутся из SpacesState. Во время мутации или необработанного результата Root проверяет актуальное состояние перед переходом и сообщает блокировку AppNavHost: rail, Back и переходы между пространствами тоже заблокированы; при удалении Root из композиции блокировка снимается.
- `DesktopEventEditorScreen.kt`, `DesktopEventFields.kt`, `DesktopEventVisibilityPanel.kt`: расширенный общий черновик EditorState, заголовок/размещение/даты/all-day/повтор/статус/локация/описание и видимость по каждому пространству. Сохранение идёт через EditorCommand, во время операции переходы блокируются как в мобильном потоке.
- `DesktopProfileScreen.kt`, `DesktopProfileSidebar.kt`, `DesktopProfileDetailsPanel.kt`, `DesktopProfileActionsPanel.kt`: широкая вкладка профиля в том же Telegram-стиле (отдельного макета профиля в SVG нет). Sidebar показывает пользователя, глобальную роль и переходы; основная область — реальные личные данные, цвет планов, число приглашений и действия безопасности из общей ProfileViewModel. Нет вымышленных устройств, online-статуса или статистики. Sidebar и основное содержимое прокручиваются независимо в низком окне. Редактирование, приглашения и управление аккаунтами открывают существующие маршруты; управление аккаунтами доступно только admin. Android и узкий Web сохраняют ProfileScreen.

`profile/ui/ProfileLogoutAllDialog.kt` — общее подтверждение выхода со всех устройств для обеих компоновок. ProfileRoot проверяет актуальный isLoggingOut перед переходами и сообщает его AppNavHost: во время выхода блокируются rail, Back и переключение вкладок, в том числе после изменения ширины. Ошибка выхода остаётся в профиле для повтора, состояние сессии и обработка cookie не изменены.

Чистые desktop-экраны имеют автономные Preview, длинные списки используют lazy-коллекции со стабильными ключами. SVG — источник оформления, не подложка UI; демонстрационные имена/даты в нём не подменяют серверные данные.

Rail доступна на основных вкладках и desktop-деталях пространства/плана и редакторе. На остальных вложенных формах (например, создание пространства, смена роли, настройки) она остаётся видимой, но отключённой: выход выполняется штатными Back/Save формы. Это не даёт обойти блокировку её операций через новую desktop-навигацию.

Все маршруты — сериализуемые наследники ScreenTypes. Только Calendar/Calendars/Profile реализуют вложенный маркер BottomBarNavigatable. Он единственный определяет видимость панели. Остальные маршруты: Registration, BoardSettings, CreateSpace, JoinSpace, Invitations, SpaceDetails/Members/MemberRole/InviteMember/Settings/LeaveSpace, ProfileEditor, EventDetails/Editor/Visibility/SpaceVisibility/Dates, Users/UserEditor. Данные между экранами передаются полями маршрутов: spaceId/userId/eventId, ISO-дата редактора и occurrenceStart деталей плана. `EventRoutes.kt` строит EventDetails из id и начала выбранного вхождения; разные вхождения одной серии имеют разные route keys. Callbacks и предметные модели в маршрутах не хранятся; даты передаются ISO-строками.

`TabNavigation.kt` сохраняет Calendar в основании и заменяет верхнюю вкладку без повторных entries. Back из Пространств/Профиля возвращает Доску. Создание/вступление открывает пространство, принятие приглашения обновляет членство; завершённое удаление/выход возвращает основную вкладку. Во время сохранения возврат блокируется. Root редактора также блокирует переходы к датам/видимости, Root деталей — к редактированию, пока операция выполняется или её результат ожидает обработки; callbacks проверяют актуальное состояние ViewModel. После сохранения/удаления плана закрывается его поток и обновляется календарь.

Одна CalendarViewModel и EventEditorViewModel создаются для сессии через rememberViewModelStoreOwner вне NavDisplay. Область переживает пересоздание Activity. Другие ViewModel принадлежат entries Navigation 3. Root подключает ViewModel, чистый UI принимает state/onEvent и callbacks. Переходы выполняются callbacks в AppNavHost с аргументами ScreenTypes; каналов и SharedFlow для навигации нет. ViewModel использует StateFlow и единую onEvent.

Завершение асинхронного сохранения/удаления остаётся в состоянии до обработки UI: Editor/ProfileEditor/BoardSettings — isSaved, UserEditor — isFinished, Calendar/Spaces — result. Root наблюдает состояние через collectAsStateWithLifecycle и LaunchedEffect, синхронно отправляет ResultHandled и вызывает актуальный callback через rememberUpdatedState. Отсутствие UI-коллектора не удаляет результат из ViewModel; повторная операция блокируется до обработки. Это состояние в памяти, без гарантии восстановления после смерти процесса. CalendarEffect и SpacesEffect теперь описывают результаты операций в состоянии, а не поток сообщений. Копирование ключа пространства выполняет Root напрямую через clipboard.

CalendarViewModel хранит дату/режим в SavedStateHandle; вкладки и открытые формы определяет стек. CalendarLoader загружает настройки и пространства, базовые серии за полные сетки предыдущего, текущего и следующего месяцев, затем разворачивает повторения. Легенда выводится из пользователей видимых планов, полный список аккаунтов для доски не запрашивается. `rememberEvent` кеширует планы из SpaceDetails и выбранные вхождения по паре id/start; `event(id, occurrenceStart)` разрешает переданные аргументы деталей, `series(id)` возвращает базовую серию для редактирования.

В пространстве SpacesViewModel разворачивает те же серии в окно текущего дня; заголовок берётся из Clock. Вхождения несут клиентские seriesStart/seriesEnd, поэтому редактирование плана из пространства, выключенного на доске, не сдвигает исходную серию. Расчёт повторений выполняется на Dispatchers.Default с проверкой отмены; для серий без COUNT перебирается только нужное окно.

`CalendarDatePager.kt` использует foundation HorizontalPager: дни в содержимом Day, недели в полосе дат Day, недели вместе с заголовком и сеткой в Week, месяцы в Month. В `CalendarPagerPeriod.kt` номер страницы привязан к календарному периоду (годы 1–9999), поэтому восстановленный PagerState не сдвигает выбранную дату. Дата меняется только после завершения свайпа (`settledPage`); Today и DatePicker позиционируют pager без обратного SelectDate. Сохраняется предпочтительный день: 31 января → конец февраля → 31 марта. Полоса недель сохраняет weekday; легенда владельцев прокручивается отдельно от Day-pager. Пустые/загружаемые/ошибочные состояния находятся внутри страниц, сам pager не удаляется при загрузке.

`CalendarState.loadedStart/loadedEndExclusive` описывают фактически загруженное окно событий. CalendarContentPager не показывает отсутствующие данные соседней страницы как пустой календарь. Выбор внутри полностью покрытого месяца переиспользует окно без запроса и повторного loading; Refresh, возврат из настроек и мутации обновляют данные принудительно. CalendarViewModel сохраняет последнюю выбранную дату при завершении in-flight загрузки и отсекает ответ отменённого запроса по loadVersion.

EventEditorViewModel объединяет Editor/Visibility/SpaceVisibility/Dates одним черновиком. Его поля и route key сохраняются в SavedStateHandle. Повторный start того же потока не сбрасывает данные; выход из Editor сбрасывает черновик. Существующему плану нельзя поменять spaceId. Форма считает all-day конец исключительно, UI показывает последний день включительно.

День: часовая сетка, all-day, пересечения и текущее время. Неделя: семь столбцов и полосы all-day, нажатие на событие открывает детали, на свободную область дня — день. Месяц: сетка, точки, выделенные диапазоны all-day и планы выбранной даты; all-day представлен компактной строкой. Повторения: DAILY/WEEKLY/MONTHLY/YEARLY, будни через BYDAY, INTERVAL/COUNT/UNTIL для поддерживаемого подмножества RRULE; месяц с отсутствующим числом и невисокосный 29 февраля пропускаются. Изменение/удаление касается всей серии, отдельных исключений нет.

День и неделя используют `core/ui/components/WhoBusyTimeGrid.kt`: часы 00–24 создаются в LazyColumn, непрерывные карточки событий размещаются отдельным слоем WhoBusyTimeGridItem. Композиция карточки существует только при пересечении с viewport; многочасовое событие остаётся видимым и доступным для нажатия после ухода его начального часа за экран. Общий scrollable на контейнере прокручивает и фон, и карточки одним LazyListState, включая жест поверх события; доступны семантические ScrollBy/ScrollToIndex. `key(resetKey)` сбрасывает позицию при смене дня/недели, а rememberLazyListState восстанавливает её при пересоздании. Нижний отступ под overlay-бар находится за границей 24:00.

`TimeGridViewport.kt` задаёт общую пиксельную геометрию линий, карточек и текущего времени: округляются абсолютные координаты начала и конца, высота равна их разнице. Подписи центрируются на линиях; минимальная высота в dp и вычитание отступа из длительности не применяются. Если субпиксельный интервал округлился в ноль, карточка показывает маркер высотой 1 px. Короткие блоки показывают только помещающийся текст либо цвет с accessibility-описанием; полные подробности доступны по нажатию. Группировка событий по дням и пересечениям кешируется через remember.

Растущие UI-коллекции используют LazyColumn/LazyRow с отдельным `items` на элементы и устойчивыми ключами: аккаунты, пространства, участники, приглашения, планы пространства, пространства в настройках доски и видимости. `EventPage` принимает LazyListScope, поэтому динамические строки видимости не оказываются внутри одного большого item. Диалоги размещения плана и выбора преемника содержат ограниченные по высоте LazyColumn.

`DayAllDayEvents.kt` и `WeekAllDayEvents.kt` отображают all-day в LazyColumn высотой до 112 dp, сохраняя место часовой сетке. Повторяющиеся события имеют ключ `id:start`; списки сбрасывают позицию при смене дня/недели. `CalendarLegend.kt` использует горизонтальный LazyRow с ключом user.id. Временные карточки Day/Week отбираются по viewport до создания UI-элементов и сохраняют непрерывную геометрию. Небольшие фиксированные наборы (3 вкладки, 7 дней, до 42 дат месяца, enum-варианты и палитра) используют обычные Row/Column.

## Права и моковые потоки

`Login/Register → AuthRepository → MockAuthRepository → MockAccountStore + SessionStore`.

`Spaces/Board/Profile/Users → WorkspaceRepository → MockWorkspaceRepository`.

`Calendar/Event → CalendarRepository → MockCalendarRepository`. Все три моковых сервиса используют общие singleton аккаунты/членство; данные новых экранов не являются изолированными заглушками.

- Глобальные роли admin/user управляют аккаунтами. Они не дают автоматического доступа к планам.
- В пространстве отдельные admin/moderator/actor/visitor. Создать собственный план может любой участник; приглашать/управлять разрешено менеджерам с ограничениями на admin. Последний администратор не может выйти, понизить или удалить себя из пространства.
- Личный план имеет shares Actors/Busy отдельно для каждого пространства; отсутствие share означает «не отправлять». Новый личный план по умолчанию отправляет Busy во все текущие пространства. Actors виден actor/moderator/admin, Busy — всем участникам. При пересечении пространств выбирается наиболее открытый уровень.
- План с spaceId виден всем участникам этого пространства, не может иметь shares. Право изменения — владелец или admin/moderator именно этого пространства.
- Мок маскирует busy-only до UI: title «Занято», description/location пусты, custom color отсутствует, shares скрыты от невладельца. Детали и кнопки редактирования используют сервероподобные busyOnly/canEdit.
- Выход/исключение удаляет shares пользователя в пространство и превращает его пространственные планы в личные. Удаление пространства применяет это ко всем планам. Последовательная история WorkspaceDetachment предотвращает потерю этих действий при быстром повторном вступлении. Удаление аккаунта удаляет его планы; для пространств назначается преемник.
- Seed: demo — admin пространства 1, actor 2, visitor 3; входящее приглашение в 4. Ключи WBTEAM2026/WBFAMILY2026/WBPROJECT2026/WBFRIENDS2026. Глобальный admin имеет отдельный аккаунт admin/admin123. Остальные демопользователи используют demo1234. Новая регистрация начинает без пространств.

## Где менять и как проверять

Все предметные пути таблицы относятся к `shared/src/commonMain/kotlin/com/project/whobusy/`.

| Задача | Места |
| --- | --- |
| UI-компонент | `core/ui/components/` + автономный Preview |
| Экран/состояние/навигация | `presentation/<feature>/{ui,viewmodel}/`, `presentation/navigation/` |
| Модели/данные/API | `domain/`, `data/{mock,remote,repository}/`, `di/`; контракт API_CONTRACT |
| Android refresh/хранилище/HTTP | Общие `data/session/` и контракты storage; реализации `shared/src/androidMain/`; SessionModule/NetworkModule |
| Даты/повторения/геометрия | `time/`, `presentation/calendar/model/`, `core/ui/components/TimeGridViewport.kt` |
| Тексты/изображения | `shared/src/commonMain/composeResources/`, `Res`, `StringResource` |
| Конфигурация платформ | `androidApp/{build.gradle.kts,src/main/}`, `shared/build.gradle.kts`, `webApp/`, каталог версий |
| Desktop Web | `presentation/desktop/ui/`, флаг desktopLayout в общих Root/AppNavHost, `docs/design/WhoBusy-Desktop-Board.svg` |
| Browser REST/Back | `shared/src/webMain/` WebAppGraph/BrowserHttpClientFactory, `shared/src/{jsMain,wasmJsMain}/` Fetch engine, общий RefreshCookiePolicy; Back/History ещё не реализованы в `presentation/platform/` |
| Экспорт Web для FunProject | `webApp/build.gradle.kts`, `webApp/deployment/funproject/{Dockerfile,.dockerignore,WHOBUSY_WEB.md}` |

```bash
./gradlew :androidApp:assembleDebug :androidApp:assembleRelease
./gradlew :shared:compileAndroidMain :shared:compileKotlinJs :shared:compileKotlinWasmJs
./gradlew :shared:testAndroidHostTest :androidApp:testDebugUnitTest
./gradlew :shared:jsNodeTest
./gradlew :androidApp:lintDebug
./gradlew :shared:assembleAndroidDeviceTest :androidApp:assembleDebugAndroidTest
./gradlew :androidApp:connectedDebugAndroidTest -PwhoBusy.useMockData=true
./gradlew :webApp:jsBrowserDevelopmentWebpack :webApp:wasmJsBrowserDevelopmentWebpack
./gradlew :webApp:jsBrowserDevelopmentRun
./gradlew :webApp:wasmJsBrowserDevelopmentRun
./gradlew :webApp:jsBrowserDistribution :webApp:wasmJsBrowserDistribution
./gradlew :webApp:exportFunProjectFrontend -PwhoBusy.web.useMockData=false -PwhoBusy.web.apiBaseUrl=
```

Release APK не подписан. `assemble…Test` только собирает инструментальные APK; `connected…Test` требует устройство/эмулятор и запускает тесты. Browser distribution публикуется целиком из `webApp/build/dist/{js,wasmJs}/productionExecutable/`, не одним JS-файлом; static hosting требует SPA fallback и same-origin `/api/` reverse proxy. Пример nginx — в README.

`exportFunProjectFrontend` зависит от `wasmJsBrowserDistribution` и создаёт `webApp/build/deploy/frontend/`: вся distribution в `static/`, nginx Dockerfile, `.dockerignore` и `WHOBUSY_WEB.md`. Папка вручную объединяется с существующей `FunProject/frontend/`: Dockerfile и `.dockerignore` заменяются, `nginx.conf`, HTTPS-шаблон и исходники React сохраняются. `static/` выбрана вместо `dist/`, потому что `frontend/dist/` исключена `.gitignore` FunProject; Dockerfile, `.dockerignore`, инструкция и вся статика сохраняются пользователем в Git, затем отправляются в репозиторий и деплоятся обычным процессом FunProject (`git pull`, `docker compose up -d --build frontend`). Базовый `docker-compose.yml` по-прежнему использует build context `./frontend`; Compose override и deploy-скрипта нет. Dockerfile собирает nginx-образ без Node.js из готовой статики и существующего `frontend/nginx.conf`; порты, API proxy и сертификаты остаются в конфигурации FunProject. HTTPS заранее настраивается там. Для следующих обновлений целиком заменяется только `frontend/static/`, а не весь `frontend`, чтобы убрать старые хешированные файлы и сохранить конфигурацию. Экспорт сам ничего не копирует, не коммитит и не деплоит. Приведённая команда явно выбирает REST и same-origin `/api/`, перекрывая пользовательский `gradle.properties`.

Web build-параметры независимы от Android: `-PwhoBusy.web.useMockData=true` явно включает demo; `-PwhoBusy.web.apiBaseUrl=https://<server>/api/` переопределяет same-origin URL. Для dev-server `WHOBUSY_API_PROXY_TARGET=https://<host>` меняет HTTPS origin прокси, без `/api` и credentials/query/fragment. Отдельный API origin требует backend CORS с конкретным разрешённым origin и credentials, а также подходящего SameSite; менять cookie в JS или отключать Secure/HttpOnly для обхода нельзя.

- `shared/src/commonTest/`: календарные границы и pager, повторения, обрезка/пересечения, формы, геометрия часовой сетки, даты/форматирование/DST, API URL, сериализация всех маршрутов без reflection и smoke-тест общего Metro-графа. DesktopLayoutTest проверяет Web-only capability и порог 1160 dp. BrowserSessionTest/RefreshCookiePolicyTest проверяют HttpOnly-политику, отсутствие JS-refresh/manual Cookie, восстановление, объединение 401 и успешный/ошибочный/отменённый logout через MockEngine, а не живой браузер. Используется kotlin.test; эти исходники входят в host-тесты, запускаются через `:shared:jsNodeTest` и компилируются для Wasm. Wasm/Skiko runtime рассчитан на браузер, поэтому Node-окружение тестов для Wasm не подключено.
- `shared/src/androidHostTest/`: существующие JVM-тесты аккаунтов, ролей/пространств/приглашений, CRUD/видимости/отсоединения, REST через Ktor MockEngine, сессии/refresh, storage, ViewModel, StateFlow результатов и аргументов маршрутов. Сервер не используется. RemoteSessionManagerTest проверяет параллельные 401, атомарную ротацию/запись, отмену, смену сервера и поколений сессии, logout-all.
- `shared/src/androidDeviceTest/.../EncryptedRefreshTokenStoreAndroidTest.kt`: настоящий Keystore и файловый DataStore, неэкспортируемый AES-256 ключ, IV, повторное открытие, отсутствие plaintext, очистка, повреждение и потеря ключа.
- `shared/src/androidDeviceTest/.../DesignGalleryTest.kt`: автономный рендер общих экранов через createComposeRule без MainActivity/DI, с доступом к внутренним preview-данным; скриншоты в external files/gallery тестового приложения, без pixel golden comparison.
- `androidApp/src/androidTest/`: AppNavigationTest, CalendarPagerTest, CalendarTimelineTest, LazyCollectionsTest и EventNavigationDuringSaveTest проверяют реальный Android UI, восстановление Activity, интервалы/скролл, lazy-элементы и переходы при сохранении. Скриншоты сохраняются в external files/screenshots.
- `androidApp/src/test/`: оставшийся host smoke-тест; предметные и ViewModel-тесты находятся в shared.

Форматирование выполняется штатным Reformat Code из Android Studio по `.idea/codeStyles/Project.xml` (`KOTLIN_OFFICIAL`) и `.editorconfig`: 4 пробела, 120 символов, trailing comma, явные импорты. Ktlint для переформатирования не используется. CLI-форматтер Studio при установленной IDE:

```bash
"$ANDROID_STUDIO_HOME/bin/format.sh" -s .idea/codeStyles/Project.xml -r -m '*.kt,*.kts' androidApp/src shared/src webApp/src build.gradle.kts settings.gradle.kts androidApp/build.gradle.kts shared/build.gradle.kts webApp/build.gradle.kts
```

Для проверки без изменения файлов добавь `-d`; не форматируй исходники одновременно со сборкой/Lint.

Корневой `.gitignore` исключает сборки/кеши, локальные SDK/env/signing-файлы, персональные настройки Studio и логи. Wrapper, исходники, документация и проектные codeStyles отслеживаются; `.env.example`/`.sample`/`.template` разрешены. Для аудита правил используй `git check-ignore --no-index -v`.

## Выполненные проверки

- Пакет для коммита в FunProject: `:webApp:exportFunProjectFrontend` прошёл с актуальной production Wasm distribution. Все 67 файлов, включая Web-логотипы, совпадают с `frontend/static/` побайтово; `.dockerignore` присутствует, overlay отсутствует. На копии `frontend` в `/tmp` проверено сохранение nginx/HTTPS-конфигурации при объединении папок; из неё собран обычный Docker-образ без overlay, выполнены `nginx -t`, HTTP 200 исходного `index.html` и проверка MIME `application/wasm`. Работающий FunProject не изменён, коммиты и реальный deploy не выполнялись. Авторизация и HTTPS этим smoke-тестом не проверялись.
- Web-брендинг: `:shared:compileAndroidMain`, `:webApp:jsBrowserDevelopmentWebpack` и production-экспорт прошли. Исходные PNG совпадают с прежними FunProject по SHA-256; favicon, Apple touch icon, startup image и preload ссылаются на существующие PNG в distribution/экспорте. В изолированном nginx все четыре прежних Web-логотипа и общий логотип входа отдаются как `200 image/png`, тела побайтово совпадают с файлами. Визуальное отображение в браузере не проверено; preload ускоряет загрузку логотипа, но не подтверждает причину его отсутствия на конкретном клиенте.
- 20.10.2026 безопасная проверка живого API: `GET https://whobusy.ru/api/health` → 200, `{"status":"ok"}`. Preflight `OPTIONS auth/login` с `Origin: http://localhost:8080` → 400 `Disallowed CORS origin`, несмотря на `Access-Control-Allow-Credentials: true`; поэтому development использует same-origin proxy, а не прямой cross-origin URL.
- Development-server `:webApp:jsBrowserDevelopmentRun` запущен в штатном REST-режиме. Через его same-origin proxy проверены `GET /api/health` → 200 и анонимный `POST /api/auth/refresh` → 401 `No session`; заголовок удаления cookie сохраняет HttpOnly, Secure, SameSite=Strict и Path=/api/auth. Сервер проверки остановлен.
- Публичный `GET https://whobusy.ru/api/openapi.json` → 200: версия 2.0.0, маршруты Auth/Board/Events/Spaces и поля Token соответствуют используемым клиентом адресам и формату access-token. Это проверка схемы, не выполнение авторизованных операций.
- Проверка health/OPTIONS не подтверждает успешный login, регистрацию или CRUD. Реальные учётные данные и пользовательские данные при этой проверке не изменялись.
- После desktop Web/browser auth выполнены `:shared:testAndroidHostTest` — 217 тестов, `:androidApp:testDebugUnitTest` — 1 тест, `:shared:jsNodeTest` — 72 теста; ошибок и пропусков нет. Browser-сценарии проверяют также смену пользователя между вкладками, отмену устаревших операций без logout новой сессии и ошибку старого access без зависшего состояния сохранения.
- Для широкой компоновки профиля прошли компиляции Android/JS/Wasm, обе Web development-webpack сборки и запуск общих JVM/JS-тестов (217/72, без ошибок и пропусков). Профиль, его панели и общий диалог имеют автономные Preview обычного/admin, отсутствующих необязательных данных, ошибки и выхода; реальный browser UI не проверен из-за отсутствия доступного браузера в CUA.
- `:shared:compileAndroidMain`, `:shared:compileKotlinJs`, `:shared:compileKotlinWasmJs` и `:shared:compileTestKotlinWasmJs` прошли. Последняя задача только компилирует Wasm-тесты, не запускает их.
- `:androidApp:assembleDebug`, `:shared:assembleAndroidDeviceTest` и `:androidApp:assembleDebugAndroidTest` прошли. Инструментальные APK только собраны, тесты на устройстве/эмуляторе в этой проверке не запускались. `:androidApp:lintDebug` — 0 ошибок, 38 предупреждений.
- `:webApp:wasmJsBrowserDistribution` прошла, включая production-оптимизацию Wasm и webpack; distribution находится в `webApp/build/dist/wasmJs/productionExecutable/`. Проверка `:webApp:jsBrowserProductionWebpack` остановлена после длительной оптимизации без результата; успешная JS production-distribution не подтверждена.
- Итоговый запуск JVM/JS-тестов, Wasm test compilation, Android APK/test APK/Lint, `:webApp:jsBrowserDevelopmentWebpack` и `:webApp:wasmJsBrowserDistribution` завершился `BUILD SUCCESSFUL`; повторные задачи использовали актуальные up-to-date результаты. JS development-бандл собран после всех изменений. Проверены локальные Markdown-ссылки, `git diff --check` и побайтовое совпадение исходного SVG с файлом из загрузок.
- Браузерная визуальная/E2E-проверка desktop и мобильной ширины не выполнена: в CUA нет доступных браузеров. Preview и проверки breakpoint не заменяют реальный UI-тест. Живой авторизованный login/CRUD без тестовой учётной записи также не проверялся.
- CLI Reformat Code Android Studio недоступен из-за read-only IDE cache/запрета Unix sockets в sandbox; Ktlint не применяется.

## Ограничения

Web использует Skiko и требует доступный WebGL2 в браузере независимо от выбора JavaScript/Wasm. Программный/HTML fallback интерфейса не реализован.

Web REST требует работоспособного same-origin API proxy либо явно разрешённого backend origin/cookie policy; сеть не заменяется моками автоматически. Синхронизация сессии между вкладками, History API, browser Back и deep links не подключены. Браузерный logout без сети не может удалить HttpOnly cookie и сохраняет текущую сессию для повтора. Android сохраняет только зашифрованный refresh; постоянный кеш планов и offline CRUD отсутствуют. Явные моковые данные сбрасываются вместе с процессом или перезагрузкой страницы.

Нет исключений отдельного повторения и переноса существующего плана между пространствами. Форматирование покрывает текущие русские шаблоны. iOS/desktop цели и их реализации не настроены. Старые методы ownerIds/VisibilityRequestDTO сохранены для совместимости; remote возвращает UnsupportedOperationException для отсутствующих API-маршрутов. Создание неактивного аккаунта выполняется POST → PATCH и не является одной серверной транзакцией.
