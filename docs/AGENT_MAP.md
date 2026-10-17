# Карта WhoBusy для ИИ-агентов

## Состояние и правила

Приложение реализует механику пространств из Figma 02 (28 экранов) и FunProject API 2.0 на моках. Модули: `app` и `core:ui`; слои приложения — пакеты `data/domain/di/presentation`. Новые данные и сессия живут только в памяти. По умолчанию нет сетевых запросов.

Перед изменениями читай относящиеся к задаче файлы. Чистые экраны и Preview не используют Hilt, сеть и data. Обновляй карту при изменении архитектуры/поведения. Контракт: [API_CONTRACT.md](API_CONTRACT.md), источник оформления и соответствие 28 экранов: [DESIGN_REFERENCE.md](DESIGN_REFERENCE.md).

## Точки входа и расположение

Пути относительно `app/src/main/java/com/project/whobusy/`, если не указано иначе.

| Область | Путь | Ответственность |
| --- | --- | --- |
| Запуск | `WhoBusyApplication.kt`, `MainActivity.kt` | Hilt, edge-to-edge, корневая Composable |
| Приложение | `presentation/WhoBusyApp.kt`, `AppViewModel.kt` | Тема, пользователь сессии, системные панели |
| Навигация | `presentation/navigation/` | Сериализуемые ScreenTypes, NavDisplay, overlay-бар, ViewModel области сессии |
| Вход | `presentation/login/ui/`, `viewmodel/` | Логин или email, пароль, ошибки, переход к регистрации |
| Регистрация | `presentation/register/ui/`, `viewmodel/` | Логин, фамилия, имя, необязательный email, пароль; успешная регистрация открывает сессию |
| Общая доска | `presentation/calendar/ui/`, `viewmodel/` | День/неделя/месяц, all-day, легенда владельцев, общие данные и мутации планов |
| Даты и геометрия | `presentation/calendar/model/` | Неделя/месяц, полосы пересечений, разворачивание повторений RecurringEvents |
| Настройки доски | `presentation/board/ui/`, `viewmodel/` | Выбор пространств и включение всех собственных планов |
| Пространства | `presentation/spaces/ui/`, `viewmodel/` | Список/пустое состояние, создание, вступление, приглашения, участники/роли, ключ, настройки, выход и удаление |
| План | `presentation/event/ui/`, `model/`, `viewmodel/` | Подробности и busy-only, общий черновик редактора, размещение, видимость, даты/весь день/статус/повторы |
| Профиль | `presentation/profile/ui/`, `viewmodel/` | Данные пользователя, приглашения, выход, изменение имени/email/цвета/пароля |
| Аккаунты | `presentation/users/ui/`, `viewmodel/` | CRUD пользователей для глобального admin; роли и активность аккаунта |
| Domain | `domain/model/`, `domain/repository/` | Модели и контракты без Android/Compose/Ktor/Hilt |
| Аккаунты моков | `data/mock/MockAccountStore.kt`, `MockAuthRepository.kt` | Общий StateFlow аккаунтов, пароли, нормализация/валидация, регистрация/вход |
| Пространства моков | `data/mock/MockWorkspaceRepository.kt`, `MockWorkspaceData.kt` | Членство, роли, приглашения, настройки доски, профиль, администрирование, история отсоединения |
| Планы моков | `data/mock/MockCalendarRepository.kt`, `MockPlanData.kt` | CRUD, права, маскировка, применение выхода/удаления пространства или аккаунта |
| Сессия | `data/session/SessionStore.kt` | Атомарные JWT и пользователь, Flow, память |
| Старый remote | `data/network/`, `data/remote/`, `data/repository/` | Ktor/OkHttp, DTO/API до появления пространств; с API 2.0 не интегрирован |
| DI | `di/NetworkModule.kt`, `CalendarModule.kt` | HttpClient/JSON/Clock, singleton моков и общий WorkspaceRepository |

`presentation/calendars/` и методы выбора ownerIds в CalendarRepository сохранены для совместимости старого исходного кода, из навигации не вызываются. Вторая вкладка имеет исторический тип `ScreenTypes.Calendars`, отображает новый `SpacesRoot`.

## Дизайн-система и строки

`core/ui/src/main/java/com/project/whobusy/core/ui/`:

- `theme/`: палитра, светлые цвета карточек, Material3-тема и типографика.
- `components/`: кнопки/FAB, поля, Tabs/BottomBar/TopBar, DateCell, EventCard/Chip, Avatar, SwitchRow, Notice, ListRow, SectionTitle, ColorPalette, SelectionMark, RadioRow, Toggle и ChoicePill.
- Предметные иконки централизованы в `components/WhoBusyIcon.kt`: `UiIcon.imageVector` использует стандартные `Icons.Filled` из Material, Back — `Icons.AutoMirrored.Filled.ArrowBack`. Маркеры выбора используют Material CheckCircle/RadioButtonUnchecked. Зависимость подключена в `core:ui`, версия — в каталоге Gradle. Исключений для текущего набора нет; новые значки сначала ищи в Material, недостающие — в готовых интернет-каталогах с указанием источника, без собственного рисования.
- Декоративные свотчи палитры и thumb переключателя используют ресурсы `core/ui/src/main/res/drawable/`. Экспорты Figma сохранены в `core/ui/src/main/assets/design/` как исходный архив оформления. Предметные иконки из него не отображаются; временных Figma URL в коде нет.
- Общие компоненты не зависят от моделей приложения. Новые визуальные компоненты и чистые экраны имеют автономные Preview со значимыми состояниями.
- Тексты приложения — `app/src/main/res/values/{strings,auth_strings,spaces_strings,board_strings,event_strings,admin_strings}.xml`; общие — `core/ui/src/main/res/values/strings.xml` с префиксом `ui_`. Ошибки состояния представлены `@StringRes Int?`; UI разрешает их через stringResource. Domain от ресурсов Android не зависит.
- `WhoBusyScreenLayout` объединяет статусный отступ, шапку и содержимое до нижней границы. `AppBottomBar` размещает панель поверх NavDisplay в прозрачном Box: 80% ширины, 64 dp высоты, радиус 25 dp, 10 dp снизу плюс navigation bar inset. Содержимое видно по бокам и ниже; у списка и FAB есть отступы для доступа к последним элементам.

## Навигация и состояния

`WhoBusyApp` наблюдает AuthRepository.currentUser. `AppNavHost` создаёт стек внутри `key(userId)`: Login для гостя, Calendar для сессии. Login → Registration; регистрация/вход меняют сессию и заменяют стек. Logout удаляет авторизованные маршруты и область ViewModel, в том числе при повторном входе того же пользователя.

Все маршруты — сериализуемые наследники ScreenTypes. Только Calendar/Calendars/Profile реализуют вложенный маркер BottomBarNavigatable. Он единственный определяет видимость панели. Остальные маршруты: Registration, BoardSettings, CreateSpace, JoinSpace, Invitations, SpaceDetails/Members/MemberRole/InviteMember/Settings/LeaveSpace, ProfileEditor, EventDetails/Editor/Visibility/SpaceVisibility/Dates, Users/UserEditor. Идентификаторы и ISO-дата передаются типизированными аргументами.

`TabNavigation.kt` сохраняет Calendar в основании и заменяет верхнюю вкладку без повторных entries. Back из Пространств/Профиля возвращает Доску. Создание/вступление открывает пространство, принятие приглашения обновляет членство; завершённое удаление/выход возвращает основную вкладку. Во время сохранения возврат блокируется. После сохранения/удаления плана закрывается его поток и обновляется календарь.

Одна CalendarViewModel и EventEditorViewModel создаются для сессии через rememberViewModelStoreOwner вне NavDisplay. Область переживает пересоздание Activity. Другие ViewModel принадлежат entries Navigation 3. Root подключает ViewModel, чистый UI принимает state/onEvent и callbacks. Одноразовые переходы передаются через Channel/Flow. ViewModel использует StateFlow и единую onEvent.

CalendarViewModel хранит дату/режим в SavedStateHandle; вкладки и открытые формы определяет стек. CalendarLoader загружает настройки и пространства, базовые серии за полную сетку месяца, затем разворачивает повторения. Легенда выводится из пользователей видимых планов, полный список аккаунтов для доски не запрашивается. Кеш `event(id)` сохраняет выбранное вхождение и планы из SpaceDetails, `series(id)` возвращает базовую серию для редактирования.

В пространстве SpacesViewModel разворачивает те же серии в окно текущего дня; заголовок берётся из Clock. Вхождения несут клиентские seriesStart/seriesEnd, поэтому редактирование плана из пространства, выключенного на доске, не сдвигает исходную серию. Расчёт повторений выполняется на Dispatchers.Default с проверкой отмены; для серий без COUNT перебирается только нужное окно.

EventEditorViewModel объединяет Editor/Visibility/SpaceVisibility/Dates одним черновиком. Его поля и route key сохраняются в SavedStateHandle. Повторный start того же потока не сбрасывает данные; выход из Editor сбрасывает черновик. Существующему плану нельзя поменять spaceId. Форма считает all-day конец исключительно, UI показывает последний день включительно.

День: часовая сетка, all-day, пересечения и текущее время. Неделя: семь столбцов и полосы all-day, нажатие на событие открывает детали, на свободную область дня — день. Месяц: сетка, точки, выделенные диапазоны all-day и планы выбранной даты; all-day представлен компактной строкой. Повторения: DAILY/WEEKLY/MONTHLY/YEARLY, будни через BYDAY, INTERVAL/COUNT/UNTIL для поддерживаемого подмножества RRULE; месяц с отсутствующим числом и невисокосный 29 февраля пропускаются. Изменение/удаление касается всей серии, отдельных исключений нет.

Короткие временные блоки в дневной сетке используют компактную карточку; полные подробности доступны на экране плана.

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
| Новые данные | domain + mock data + DI; remote отдельно с актуальным API-контрактом |
| Даты/повторения/геометрия | calendar/model, соответствующие JVM-тесты |
| Тексты | values/*.xml соответствующей фичи; stringResource в UI |
| Зависимости | gradle/libs.versions.toml, build.gradle.kts модулей |
| SDK/сборка | app/build.gradle.kts, core/ui/build.gradle.kts, wrapper, daemon JVM properties |

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
./gradlew :app:assembleDebugAndroidTest
./gradlew :app:connectedDebugAndroidTest
```

JVM-тесты в `app/src/test/` проверяют аккаунты/нормализацию/пароли, пространства/роли/приглашения/последнего администратора, видимость/CRUD/отсоединение планов, повторения и даты, ViewModel/эффекты/черновик, сериализацию маршрутов. Исторические MockEngine-тесты проверяют старый remote и не доказывают интеграцию API 2.0.

Android-сценарии: `app/src/androidTest/.../AppNavigationTest.kt` — шесть потоков регистрации/доски/пространств/приглашений/планов/профиля/аккаунтов, восстановление Activity, видимость и геометрия overlay. Helper сохраняет PNG в external files/screenshots; для выгрузки запускай instrumentation после установки APK и забирай файлы до удаления приложения Gradle. `DesignGalleryTest#galleryAllScreens` отдельно отрисовывает 28 состояний на автономных данных, сохраняет PNG в external files/gallery; это визуальная проверка, без pixel golden comparison.

Форматирование `.kt` и `.kts` выполняется штатным Reformat Code из Android Studio (у пользователя назначен на Ctrl+S). `.idea/codeStyles/` закрепляет `KOTLIN_OFFICIAL`; `.editorconfig` задаёт 4 пробела, 120 символов, trailing comma и явные импорты. Ktlint не используется для переформатирования, поскольку иначе расставляет переносы. Для [CLI-форматтера Studio](https://www.jetbrains.com/help/idea/command-line-formatter.html) укажи путь к установленной IDE в `ANDROID_STUDIO_HOME`:
```bash
"$ANDROID_STUDIO_HOME/bin/format.sh" -s .idea/codeStyles/Project.xml -r -m '*.kt,*.kts' app/src core/ui/src build.gradle.kts settings.gradle.kts app/build.gradle.kts core/ui/build.gradle.kts
```
Для проверки без изменения файлов добавь `-d`; повторный запуск после форматирования должен завершаться без различий. Не форматируй исходники одновременно со сборкой или Lint.

Корневой `.gitignore` исключает сборки и кеши всех модулей, локальные SDK/env/signing-файлы, персональные настройки Studio, логи и дампы. Gradle Wrapper, `.editorconfig`, `.idea/codeStyles/`, исходники и документация остаются доступными для коммита; примеры `.env.example`/`.env.sample`/`.env.template` разрешены. Правила проверяются через `git check-ignore --no-index -v`. Уже отслеживаемый `.idea/deploymentTargetSelector.xml` остаётся в индексе: `.gitignore` не прекращает отслеживание ранее добавленных файлов.

## Выполненные проверки

- Debug APK и APK инструментальных тестов собраны: `:app:assembleDebug`, `:app:assembleDebugAndroidTest`.
- `:app:testDebugUnitTest`: запущены 79 JVM-тестов, все прошли.
- На временном эмуляторе Android 15 / API 35 запущены шесть сценариев AppNavigationTest и DesignGalleryTest: всего 7 тестов, все прошли. Галерея отрисовала 28 состояний; PNG просмотрены вручную, включая overlay, формы, busy-only и диалог удаления. Размер экрана при проверке — 390 × 844 dp.
- Android Lint: 0 ошибок, 70 предупреждений (преимущественно неиспользуемые ресурсы и сведения о версиях).
- Штатный форматтер Android Studio 2026.1.2 обработал все 204 файла `.kt`/`.kts`; повторная проверка `-d` прошла. Сравнение Kotlin-токенов со снимком до форматирования не обнаружило изменений вне пробельных символов; `git diff --check` прошёл. После форматирования повторно собран Debug APK и запущены 79 JVM-тестов; `:app:compileDebugAndroidTestKotlin` скомпилировал Android-тесты без их запуска.
- Material-иконки проверены сборкой Debug и AndroidTest APK и Lint; отдельно запущен `DesignGalleryTest` (1 Android-тест, 28 состояний), все изображения отрисованы и просмотрены. Функциональный прогон и JVM-тесты выше относятся к проверке механики приложения.
- Путь каждого экрана в DESIGN_REFERENCE и локальные ссылки документации проверены. API-контракт сверен с 31 бизнес-маршрутом FunProject commit `1cfd95b`; живой API и backend-тесты не запускались.

Сборка AndroidTest APK сама по себе не запускает тесты. Фактический прогон выше выполнен через `adb shell am instrument -w -e class com.project.whobusy.DesignGalleryTest,com.project.whobusy.AppNavigationTest com.project.whobusy.test/androidx.test.runner.AndroidJUnitRunner`.

## Ограничения

Нет постоянного хранилища, восстановления авторизации после смерти процесса, исключений отдельного повторения и живой интеграции новых API. USE_MOCK_DATA по умолчанию true. Старые remote DTO/маршруты необходимо мигрировать до использования API 2.0; WorkspaceRepository сейчас предоставляет мок. Живой backend в рамках задачи не вызывается.
