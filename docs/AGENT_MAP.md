# Карта WhoBusy для ИИ-агентов

## Состояние и правила

Приложение реализует механику пространств из Figma 02 (28 экранов) и FunProject API 2.0. Модули: `app` и `core:ui`; слои приложения — пакеты `data/domain/di/presentation`. Auth/Calendar/Workspace выбираются согласованно через BuildConfig.USE_MOCK_DATA. Сейчас в `app/build.gradle.kts` задано `useMockData = false` (REST), чтение свойства `whoBusy.useMockData` закомментировано. Моки хранят данные в памяти без сети. Штатный адрес REST — `https://whobusy.ru/api/`, переопределяется `-PwhoBusy.apiBaseUrl=https://<server>/api/`.

Перед изменениями читай относящиеся к задаче файлы. Чистые экраны и Preview не используют Hilt, сеть и data. Обновляй карту при изменении архитектуры/поведения. Контракт: [API_CONTRACT.md](API_CONTRACT.md), источник оформления и соответствие 28 экранов: [DESIGN_REFERENCE.md](DESIGN_REFERENCE.md).

## Точки входа и расположение

Пути относительно `app/src/main/java/com/project/whobusy/`, если не указано иначе.

| Область | Путь | Ответственность |
| --- | --- | --- |
| Запуск | `WhoBusyApplication.kt`, `MainActivity.kt` | Hilt, edge-to-edge, корневая Composable |
| Приложение | `presentation/WhoBusyApp.kt`, `AppViewModel.kt`, `AppState.kt`, `AppCommand.kt`, `session/ui/SessionRestoreScreen.kt` | Тема, пользователь сессии, восстановление входа, повтор/новый вход, системные панели |
| Навигация | `presentation/navigation/` | Сериализуемые ScreenTypes, NavDisplay, overlay-бар, ViewModel области сессии |
| Вход | `presentation/login/ui/`, `viewmodel/` | Логин или email, пароль, ошибки, переход к регистрации |
| Регистрация | `presentation/register/ui/`, `viewmodel/` | Логин, фамилия, имя, необязательный email, пароль; успешная регистрация открывает сессию |
| Общая доска | `presentation/calendar/ui/`, `viewmodel/` | День/неделя/месяц, all-day, легенда владельцев, общие данные и мутации планов |
| Даты и геометрия | `presentation/calendar/model/` | Неделя/месяц, полосы пересечений, разворачивание повторений RecurringEvents |
| Настройки доски | `presentation/board/ui/`, `viewmodel/` | Выбор пространств и включение всех собственных планов |
| Пространства | `presentation/spaces/ui/`, `viewmodel/` | Список/пустое состояние, создание, вступление, приглашения, участники/роли, ключ, настройки, выход и удаление |
| План | `presentation/event/ui/`, `model/`, `viewmodel/` | Подробности и busy-only, общий черновик редактора, размещение, видимость, даты/весь день/статус/повторы |
| Профиль | `presentation/profile/ui/`, `viewmodel/` | Данные пользователя, приглашения, выход с текущего/всех устройств, изменение имени/email/цвета/пароля |
| Аккаунты | `presentation/users/ui/`, `viewmodel/` | CRUD пользователей для глобального admin; роли и активность аккаунта |
| Domain | `domain/model/`, `domain/repository/` | Модели и контракты без Android/Compose/Ktor/Hilt |
| Аккаунты моков | `data/mock/MockAccountStore.kt`, `MockAuthRepository.kt` | Общий StateFlow аккаунтов, пароли, нормализация/валидация, регистрация/вход |
| Пространства моков | `data/mock/MockWorkspaceRepository.kt`, `MockWorkspaceData.kt` | Членство, роли, приглашения, настройки доски, профиль, администрирование, история отсоединения |
| Планы моков | `data/mock/MockCalendarRepository.kt`, `MockPlanData.kt` | CRUD, права, маскировка, применение выхода/удаления пространства или аккаунта |
| Сессия | `data/session/SessionStore.kt`, `RemoteSessionManager.kt`, `storage/` | Access/user в памяти, refresh в зашифрованном DataStore, ключ Android Keystore, ротация/выход/восстановление |
| REST | `data/network/`, `data/remote/`, `data/repository/` | HTTPS Ktor/OkHttp, Auth/Calendar/Workspace API и DTO, реальные реализации всех трёх контрактов |
| DI | `di/NetworkModule.kt`, `CalendarModule.kt`, `SessionModule.kt` | HttpClient/JSON/Clock, единый режим mock/remote, singleton защищённого хранилища и application CoroutineScope |

`presentation/calendars/` и методы выбора ownerIds в CalendarRepository сохранены для совместимости старого исходного кода, из навигации не вызываются. Вторая вкладка имеет исторический тип `ScreenTypes.Calendars`, отображает новый `SpacesRoot`.

## Дизайн-система и строки

`core/ui/src/main/java/com/project/whobusy/core/ui/`:

- `theme/`: палитра, светлые цвета карточек, Material3-тема и типографика.
- `components/`: кнопки/FAB, поля, Tabs/BottomBar/TopBar, DateCell, EventCard/Chip, Avatar, SwitchRow, Notice, ListRow, SectionTitle, ColorPalette, SelectionMark, RadioRow, Toggle, ChoicePill и общая часовая сетка WhoBusyTimeGrid/WhoBusyTimeGridItem.
- Предметные иконки централизованы в `components/WhoBusyIcon.kt`: `UiIcon.imageVector` использует стандартные `Icons.Filled` из Material, Back — `Icons.AutoMirrored.Filled.ArrowBack`. Маркеры выбора используют Material CheckCircle/RadioButtonUnchecked. Зависимость подключена в `core:ui`, версия — в каталоге Gradle. Исключений для текущего набора нет; новые значки сначала ищи в Material, недостающие — в готовых интернет-каталогах с указанием источника, без собственного рисования.
- Декоративные свотчи палитры и thumb переключателя используют ресурсы `core/ui/src/main/res/drawable/`. Экспорты Figma сохранены в `core/ui/src/main/assets/design/` как исходный архив оформления. Предметные иконки из него не отображаются; временных Figma URL в коде нет.
- `WhoBusyTextField` имеет стили Plain и Filled. Вход и регистрация используют Filled: голубой primaryContainer, синяя обводка 1 dp, тёмные подписи и placeholder; ошибка окрашивает обводку и подпись в error. Остальные формы сохраняют Plain. Автономный AuthTextFieldsPreview показывает пустое, заполненное, ошибочное и disabled-состояния.
- Общие компоненты не зависят от моделей приложения. Новые визуальные компоненты и чистые экраны имеют автономные Preview со значимыми состояниями.
- Логотип — исходный пользовательский `WhoBusy.png`, без изменений сохранённый в `core/ui/src/main/res/drawable-nodpi/whobusy_logo.png`. `components/WhoBusyLogo.kt` показывает его на экране входа без tint и имеет Preview. Календарная иллюстрация пустой доски остаётся отдельным WhoBusyCalendarBadge.
- Значок запуска задан в `app/src/main/AndroidManifest.xml` через `mipmap-anydpi/ic_launcher{,_round}.xml`: оба используют PNG из core:ui, белый фон и отступ foreground 12% для масок Android. Файлы слоёв — `app/src/main/res/drawable/ic_launcher_{foreground,background}.xml`. При minSdk 34 используются адаптивные XML; старые density-specific WebP сохранены и не являются источником текущего логотипа. Цветной PNG не используется как монохромная маска.
- Тексты приложения — `app/src/main/res/values/{strings,auth_strings,spaces_strings,board_strings,event_strings,admin_strings}.xml`; общие — `core/ui/src/main/res/values/strings.xml` с префиксом `ui_`. Ошибки состояния представлены `@StringRes Int?`; UI разрешает их через stringResource. Domain от ресурсов Android не зависит.
- `WhoBusyScreenLayout` объединяет статусный отступ, шапку и содержимое до нижней границы. `AppBottomBar` размещает панель поверх NavDisplay в прозрачном Box: 80% ширины, 64 dp высоты, радиус 25 dp, 10 dp снизу плюс navigation bar inset. Содержимое видно по бокам и ниже; у списка и FAB есть отступы для доступа к последним элементам.

## Навигация и состояния

`AppViewModel` объединяет пользователя AuthRepository.currentUser и startup Loading/Ready/Failed в AppState. До завершения restoreSession навигация не показывается: SessionRestoreScreen отображает загрузку либо повтор/вход заново. Ошибка сети оставляет зашифрованный refresh для повтора; невалидный/отозванный refresh приводит к гостевому входу. `AppNavHost` создаёт стек внутри `key(userId)`: Login для гостя, Calendar для сессии. Login → Registration; регистрация/вход меняют сессию и заменяют стек. Logout удаляет авторизованные маршруты и область ViewModel, в том числе при повторном входе того же пользователя.

## Авторизация и защищённое хранение

`AuthRepository.logout()` теперь suspend и возвращает Result: состояние гостя публикуется после очистки DataStore. RemoteAuthRepository поддерживает login/register, restoreSession, logout и logoutAll. В ProfileScreen отдельная кнопка выхода на всех устройствах открывает подтверждение; ProfileViewModel блокирует повторный запрос, закрытие диалога и переходы во время выполнения. Ошибка logout-all оставляет текущую сессию и показывает сообщение. Моки не создают постоянную сессию и не обращаются к сети; оба вида выхода очищают только локальную моковую сессию.

`AuthAPI` получает access_token/token_type/expires_in из JSON и значение refresh_token из Set-Cookie. Access JWT и пользователь остаются только в SessionStore. В EncryptedRefreshTokenStore записывается зашифрованный JSON с refresh и точным base URL; при смене сервера прежний токен не отправляется на другой адрес. Пароли, access и расшифровочный ключ в DataStore не хранятся.

`storage/AndroidKeystoreTokenCipher.kt` создаёт неэкспортируемый AES-256 ключ AndroidKeyStore; `AesGcmTokenCipher.kt` использует AES/GCM/NoPadding, случайный 96-битный IV, 128-битный authentication tag и AAD с назначением/версией. Preferences DataStore хранит только byte[] envelope magic/version/IV/ciphertext/tag в `Context.noBackupFilesDir/datastore/auth_session.preferences_pb`; файл не переносится backup/device transfer. Криптография выполняется на IO. Повреждение файла/шифротекста или потеря ключа сбрасывает сохранённое значение, без хранения plaintext и без генерации нового ключа при чтении старого ciphertext. Новый ключ создаётся только при записи.

`RemoteSessionManager` сериализует обновление через Mutex. AuthenticatedHttpClient запрашивает действующий access, при 401 обновляет refresh и повторяет запрос один раз; 403/сетевые ошибки не запускают повтор мутации. Версия обновления позволяет объединить параллельные 401 даже при одинаковом JWT; generation не даёт ответу старой сессии затронуть новую. Ротация, фиксация нового refresh и публикация access защищены NonCancellable IO-транзакцией от отмены вызывающего coroutine. После повторного 401/отозванного refresh локальная сессия очищается.

Клиент вручную передаёт refresh только в auth/refresh, auth/logout и PATCH auth/me (смена пароля должна сохранить текущую семью сессии); access/refresh для PATCH берутся одной атомарной парой. Это ограниченный transport cookie value, не общий persistent CookieJar. HTTPS обязателен, редиректы отключены. Обычный выход сначала завершает локальную durable-очистку; серверный отзыв выполняется best effort в application scope. Logout-all выполняется сериализованно и очищает локальную сессию после успешного ответа. Access-токены уже выпущенной сессии сервер допускает до exp, если аккаунт не заблокирован/удалён.

После смерти процесса новый access и пользователь загружаются через refresh. Аварийное завершение после серверной ротации, но до получения/записи нового refresh, остаётся ограничением протокола: сервер даёт 30 секунд grace старому токену, затем повтор старого токена отзывает семью. Отмена coroutine закрыта транзакцией клиента; атомарность с удалённым сервером не заявляется.

## Календарь и формы

Все маршруты — сериализуемые наследники ScreenTypes. Только Calendar/Calendars/Profile реализуют вложенный маркер BottomBarNavigatable. Он единственный определяет видимость панели. Остальные маршруты: Registration, BoardSettings, CreateSpace, JoinSpace, Invitations, SpaceDetails/Members/MemberRole/InviteMember/Settings/LeaveSpace, ProfileEditor, EventDetails/Editor/Visibility/SpaceVisibility/Dates, Users/UserEditor. Данные между экранами передаются полями маршрутов: spaceId/userId/eventId, ISO-дата редактора и occurrenceStart деталей плана. `EventRoutes.kt` строит EventDetails из id и начала выбранного вхождения; разные вхождения одной серии имеют разные route keys. Callbacks и модели с java.time в маршрутах не хранятся.

`TabNavigation.kt` сохраняет Calendar в основании и заменяет верхнюю вкладку без повторных entries. Back из Пространств/Профиля возвращает Доску. Создание/вступление открывает пространство, принятие приглашения обновляет членство; завершённое удаление/выход возвращает основную вкладку. Во время сохранения возврат блокируется. Root редактора также блокирует переходы к датам/видимости, Root деталей — к редактированию, пока операция выполняется или её результат ожидает обработки; callbacks проверяют актуальное состояние ViewModel. После сохранения/удаления плана закрывается его поток и обновляется календарь.

Одна CalendarViewModel и EventEditorViewModel создаются для сессии через rememberViewModelStoreOwner вне NavDisplay. Область переживает пересоздание Activity. Другие ViewModel принадлежат entries Navigation 3. Root подключает ViewModel, чистый UI принимает state/onEvent и callbacks. Переходы выполняются callbacks в AppNavHost с аргументами ScreenTypes; каналов и SharedFlow для навигации нет. ViewModel использует StateFlow и единую onEvent.

Завершение асинхронного сохранения/удаления остаётся в состоянии до обработки UI: Editor/ProfileEditor/BoardSettings — isSaved, UserEditor — isFinished, Calendar/Spaces — result. Root наблюдает состояние через collectAsStateWithLifecycle и LaunchedEffect, синхронно отправляет ResultHandled и вызывает актуальный callback через rememberUpdatedState. Отсутствие UI-коллектора не удаляет результат из ViewModel; повторная операция блокируется до обработки. Это состояние в памяти, без гарантии восстановления после смерти процесса. CalendarEffect и SpacesEffect теперь описывают результаты операций в состоянии, а не поток сообщений. Копирование ключа пространства выполняет Root напрямую через clipboard.

CalendarViewModel хранит дату/режим в SavedStateHandle; вкладки и открытые формы определяет стек. CalendarLoader загружает настройки и пространства, базовые серии за полные сетки предыдущего, текущего и следующего месяцев, затем разворачивает повторения. Легенда выводится из пользователей видимых планов, полный список аккаунтов для доски не запрашивается. `rememberEvent` кеширует планы из SpaceDetails и выбранные вхождения по паре id/start; `event(id, occurrenceStart)` разрешает переданные аргументы деталей, `series(id)` возвращает базовую серию для редактирования.

В пространстве SpacesViewModel разворачивает те же серии в окно текущего дня; заголовок берётся из Clock. Вхождения несут клиентские seriesStart/seriesEnd, поэтому редактирование плана из пространства, выключенного на доске, не сдвигает исходную серию. Расчёт повторений выполняется на Dispatchers.Default с проверкой отмены; для серий без COUNT перебирается только нужное окно.

`CalendarDatePager.kt` использует foundation HorizontalPager: дни в содержимом Day, недели в полосе дат Day, недели вместе с заголовком и сеткой в Week, месяцы в Month. В `CalendarPagerPeriod.kt` номер страницы привязан к календарному периоду (годы 1–9999), поэтому восстановленный PagerState не сдвигает выбранную дату. Дата меняется только после завершения свайпа (`settledPage`); Today и DatePicker позиционируют pager без обратного SelectDate. Сохраняется предпочтительный день: 31 января → конец февраля → 31 марта. Полоса недель сохраняет weekday; легенда владельцев прокручивается отдельно от Day-pager. Пустые/загружаемые/ошибочные состояния находятся внутри страниц, сам pager не удаляется при загрузке.

`CalendarState.loadedStart/loadedEndExclusive` описывают фактически загруженное окно событий. CalendarContentPager не показывает отсутствующие данные соседней страницы как пустой календарь. Выбор внутри полностью покрытого месяца переиспользует окно без запроса и повторного loading; Refresh, возврат из настроек и мутации обновляют данные принудительно. CalendarViewModel сохраняет последнюю выбранную дату при завершении in-flight загрузки и отсекает ответ отменённого запроса по loadVersion.

EventEditorViewModel объединяет Editor/Visibility/SpaceVisibility/Dates одним черновиком. Его поля и route key сохраняются в SavedStateHandle. Повторный start того же потока не сбрасывает данные; выход из Editor сбрасывает черновик. Существующему плану нельзя поменять spaceId. Форма считает all-day конец исключительно, UI показывает последний день включительно.

День: часовая сетка, all-day, пересечения и текущее время. Неделя: семь столбцов и полосы all-day, нажатие на событие открывает детали, на свободную область дня — день. Месяц: сетка, точки, выделенные диапазоны all-day и планы выбранной даты; all-day представлен компактной строкой. Повторения: DAILY/WEEKLY/MONTHLY/YEARLY, будни через BYDAY, INTERVAL/COUNT/UNTIL для поддерживаемого подмножества RRULE; месяц с отсутствующим числом и невисокосный 29 февраля пропускаются. Изменение/удаление касается всей серии, отдельных исключений нет.

День и неделя используют `core:ui/components/WhoBusyTimeGrid.kt`: часы 00–24 создаются в LazyColumn, непрерывные карточки событий размещаются отдельным слоем WhoBusyTimeGridItem. Композиция карточки существует только при пересечении с viewport; многочасовое событие остаётся видимым и доступным для нажатия после ухода его начального часа за экран. Общий scrollable на контейнере прокручивает и фон, и карточки одним LazyListState, включая жест поверх события; доступны семантические ScrollBy/ScrollToIndex. `key(resetKey)` сбрасывает позицию при смене дня/недели, а rememberLazyListState восстанавливает её при пересоздании. Нижний отступ под overlay-бар находится за границей 24:00.

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

| Задача | Места |
| --- | --- |
| Новый UI-компонент | core:ui/components + автономный Preview |
| Новый экран | presentation/feature/ui + viewmodel/State/Command; маршрут navigation |
| Новые данные | domain + mock data + remote + DI; сверяй DTO и маршруты с API-контрактом |
| Авторизация/refresh | data/session, storage, AuthAPI, AuthenticatedHttpClient, SessionModule; JVM и Android storage-тесты |
| Даты/повторения/геометрия | calendar/model, соответствующие JVM-тесты |
| Тексты | values/*.xml соответствующей фичи; stringResource в UI |
| Зависимости | gradle/libs.versions.toml, build.gradle.kts модулей |
| SDK/сборка | app/build.gradle.kts, core/ui/build.gradle.kts, wrapper, daemon JVM properties |

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
./gradlew :core:ui:testDebugUnitTest
./gradlew :app:assembleDebugAndroidTest
./gradlew :app:connectedDebugAndroidTest
```

JVM-тесты в `app/src/test/` проверяют аккаунты/нормализацию/пароли, пространства/роли/приглашения/последнего администратора, видимость/CRUD/отсоединение планов, повторения и даты, ViewModel/состояния завершения/черновик, сериализацию аргументов маршрутов и разные вхождения одной серии. Проверяются сохранность результата без UI, подтверждение ResultHandled и блокировка повторной операции до подтверждения. MockEngine-тесты RemoteAuth/Calendar/Workspace проверяют актуальные DTO, cookie, маршруты, фильтры, права и обработку ошибок API 2.0. RemoteSessionManagerTest проверяет объединение параллельных 401, ротацию при отмене coroutine, привязку refresh к серверу, согласованность пары access/refresh, отклонение ответа старой сессии и сериализацию logout-all с новым входом. AppViewModelTest покрывает восстановление/повтор/новый вход, ProfileViewModelTest — подтверждение, ошибку и блокировку повторного выхода. Эти тесты используют MockEngine, а не живой сервер.

Android-сценарии: `app/src/androidTest/.../AppNavigationTest.kt` — шесть потоков регистрации/доски/пространств/приглашений/планов/профиля/аккаунтов, восстановление Activity, видимость и геометрия overlay. Проверяется пересоздание открытых деталей/редактора, сохранение профиля и плана и отсутствие повторного возврата после пересоздания Activity. Helper сохраняет PNG в external files/screenshots; для выгрузки запускай instrumentation после установки APK и забирай файлы до удаления приложения Gradle. `DesignGalleryTest#galleryAllScreens` отдельно отрисовывает 28 состояний на автономных данных, сохраняет PNG в external files/gallery; это визуальная проверка, без pixel golden comparison.

`CalendarPagerTest.kt` проверяет реальные свайпы всех четырёх pager, переходы вперёд/назад, выбор даты и режима, Today, восстановление после свайпа, конец февраля и независимость вертикальной прокрутки часов. CalendarPagerPeriodTest проверяет привязку дат к страницам и календарные границы; CalendarLoaderTest/CalendarViewModelTest — полное окно соседних месяцев, переиспользование данных и гонки загрузок.

`CalendarTimelineTest.kt` проверяет реальные границы карточек дня/недели относительно линий часов, 15-минутный интервал и конец в 24:00, непрерывность многочасовой карточки, нажатие и прокрутку поверх неё, исключение невидимых событий, восстановление прокрутки через StateRestorationTester. Чистая геометрия и округление покрыты `core/ui/src/test/.../TimeGridViewportTest.kt`; обрезка многодневного события — CalendarDatesTest.

`LazyCollectionsTest.kt` использует 100 all-day событий, 100 владельцев и 200 аккаунтов: дальние элементы отсутствуют в композиции до прокрутки, после неё доступны для нажатия, а часовая сетка остаётся видимой.

`data/session/storage/EncryptedRefreshTokenStoreAndroidTest.kt` запускается с настоящим Android Keystore и файловым DataStore: проверяет неэкспортируемый AES-256 ключ, разные IV, повторное открытие хранилища, отсутствие plaintext в файле, очистку, повреждение данных и потерю ключа. JVM-тесты storage отдельно проверяют AES-GCM envelope и транзакции DataStore.

`EventNavigationDuringSaveTest.kt` удерживает сохранение/удаление через CompletableDeferred в тестовом CalendarRepository: реальные Root не вызывают переходы к датам/видимости/редактированию и возврат, пока операция выполняется. После завершения результат подтверждается и callback вызывается один раз.

Форматирование `.kt` и `.kts` выполняется штатным Reformat Code из Android Studio (у пользователя назначен на Ctrl+S). `.idea/codeStyles/` закрепляет `KOTLIN_OFFICIAL`; `.editorconfig` задаёт 4 пробела, 120 символов, trailing comma и явные импорты. Ktlint не используется для переформатирования, поскольку иначе расставляет переносы. Для [CLI-форматтера Studio](https://www.jetbrains.com/help/idea/command-line-formatter.html) укажи путь к установленной IDE в `ANDROID_STUDIO_HOME`:
```bash
"$ANDROID_STUDIO_HOME/bin/format.sh" -s .idea/codeStyles/Project.xml -r -m '*.kt,*.kts' app/src core/ui/src build.gradle.kts settings.gradle.kts app/build.gradle.kts core/ui/build.gradle.kts
```
Для проверки без изменения файлов добавь `-d`; повторный запуск после форматирования должен завершаться без различий. Не форматируй исходники одновременно со сборкой или Lint.

Корневой `.gitignore` исключает сборки и кеши всех модулей, локальные SDK/env/signing-файлы, персональные настройки Studio, логи и дампы. Gradle Wrapper, `.editorconfig`, `.idea/codeStyles/`, исходники и документация остаются доступными для коммита; примеры `.env.example`/`.env.sample`/`.env.template` разрешены. Правила проверяются через `git check-ignore --no-index -v`. Уже отслеживаемый `.idea/deploymentTargetSelector.xml` остаётся в индексе: `.gitignore` не прекращает отслеживание ранее добавленных файлов.

## Выполненные проверки

- Debug APK и APK инструментальных тестов собраны: `:app:assembleDebug`, `:app:assembleDebugAndroidTest`. Проверенная сборка использует текущий REST-режим из app/build.gradle.kts; UI-тесты календаря задают автономные данные без Hilt/сети.
- `:app:testDebugUnitTest` и `:core:ui:testDebugUnitTest`: запущены 174 и 7 JVM-тестов соответственно, все прошли.
- На временном эмуляторе Android 15 / API 35 запущены 8 CalendarPagerTest, 6 CalendarTimelineTest, 4 LazyCollectionsTest и 1 DesignGalleryTest: всего 19 Android-тестов, все прошли. Проверены четыре области горизонтального листания, Today/выбор даты и режима, восстановление после свайпа, конец февраля, независимость вертикальных жестов, геометрия часов и виртуализация длинных списков. Галерея отрисовала 28 состояний; день, неделя и месяц просмотрены вручную при 390 × 844 dp. Настоящие Keystore/DataStore и полная навигация дополнительно проверены предыдущим прогоном из 26 Android-тестов при интеграции API.
- Для оформления полей входа/регистрации заново собраны `:app:assembleDebug` и `:app:assembleDebugAndroidTest`, затем запущен `DesignGalleryTest#galleryAllScreens`: 1 Android-тест прошёл, отрисованы 28 состояний. Вход и регистрация просмотрены вручную при 390 × 844 dp. Три изменённых Kotlin-файла прошли штатный formatter Studio в режиме `-d`; `git diff --check` прошёл. Новые автоматические тесты для смены цветов не добавлялись; JVM-тесты в этой визуальной правке повторно не запускались.
- Логотип проверен в собранном Debug APK на временном эмуляторе: экран входа и круглый значок launcher просмотрены вручную, буквы и лупа не обрезаются. SHA-256 ресурса совпадает с исходным пользовательским PNG. Отдельные автоматические тесты для замены изображения не добавлялись и не запускались.
- Android Lint: 0 ошибок, 71 предупреждение (преимущественно неиспользуемые ресурсы и сведения о версиях; также отсутствие отдельной монохромной маски логотипа).
- Форматирование всех 255 Kotlin-файлов проверено штатным форматтером Android Studio 2026.1.2 в режиме `-d`; `git diff --check` прошёл. Отдельная проверка Gradle-скриптов сообщает Needs reformatting для неизменённого app/build.gradle.kts; форматтер не создаёт diff этого файла.
- На `https://whobusy.ru/api/` с отдельным непривилегированным тестовым аккаунтом выполнены регистрация, чтение профиля/пространств/приглашений/настроек доски/планов, ротация refresh и logout с последующим 401. В настоящей сетевой Android-сборке проверены вход и восстановление после завершения процесса; файл DataStore содержит шифротекст без маркеров plaintext. Проверены отмена подтверждения logout-all, успешный выход, отказ обновления второй сессии (401) и сохранение гостевого состояния после перезапуска. Административные операции и мутации пространств на живом сервисе не проверялись; для них выполнены MockEngine-тесты и сверка исходников.
- Путь каждого экрана в DESIGN_REFERENCE и локальные ссылки документации проверены. API-контракт сверен с 34 бизнес-маршрутами FunProject commit `f828103`; live OpenAPI/health доступны по `https://whobusy.ru/api/`. Backend-тесты не запускались.

Сборка AndroidTest APK сама по себе не запускает тесты. Последний фактический прогон: `adb -s emulator-5554 shell am instrument -w -e class com.project.whobusy.DesignGalleryTest#galleryAllScreens com.project.whobusy.test/androidx.test.runner.AndroidJUnitRunner`.

## Ограничения

Моковые данные остаются в памяти; постоянный пользовательский кеш и offline CRUD не реализованы. В remote сохраняется только зашифрованный refresh, остальное повторно загружается с сервера. Нет исключений отдельного повторения и переноса существующего плана между пространствами. Текущая сборка использует REST (`useMockData = false` в app/build.gradle.kts); свойство переключения режима сейчас закомментировано. Старые методы выбора ownerIds и VisibilityRequestDTO сохранены для совместимости, из текущей навигации не используются; remote возвращает UnsupportedOperationException вместо обращений к отсутствующим маршрутам. Создание неактивного аккаунта выполняется POST → PATCH и не является одной серверной транзакцией.
