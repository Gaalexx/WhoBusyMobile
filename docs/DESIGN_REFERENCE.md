# Источник интерфейса

Мобильный макет: [WhoBusy · Пространства, страница 02](https://www.figma.com/design/dDatbSFzoHbGIn2PWUqZnx?node-id=14-90).
Общий интерфейс в `shared` реализует 28 экранов этой страницы и используется Android-host `androidApp` и узким браузерным `webApp`. Для широкого Web источник — пользовательский `WhoBusy-Desktop-Board.svg` из Downloads, неизменённая копия в [docs/design/WhoBusy-Desktop-Board.svg](design/WhoBusy-Desktop-Board.svg), 139 685 байт. Desktop-компоновка использует общие состояния/команды и реальные репозитории; демоданные включаются только явно. Контекст мобильных слоёв и screenshots получен через Figma MCP; прежняя страница 0:1 и архив FunProject-Mobile-Figma.zip описывают старую механику календарей коллег.

## Соответствие экранов

Пути относительно `shared/src/commonMain/kotlin/com/project/whobusy/presentation/`.

| № / Figma node | Состояние | Реализация |
| --- | --- | --- |
| 01 / 14:190 | Вход по логину или email | `login/ui/LoginScreen.kt` |
| 02 / 14:212 | Регистрация | `register/ui/RegistrationScreen.kt` |
| 03 / 14:244 | Общая доска · день | `calendar/ui/CalendarScreen.kt`, `calendar/ui/DayCalendar.kt` |
| 04 / 14:334 | Настройки доски | `board/ui/BoardSettingsScreen.kt` |
| 05 / 14:389 | Общая доска · неделя | `calendar/ui/WeekCalendar.kt` |
| 06 / 14:488 | Общая доска · месяц | `calendar/ui/MonthCalendar.kt` |
| 07 / 14:592 | Мои пространства | `spaces/ui/SpacesScreen.kt` |
| 08 / 14:647 | Создать пространство | `spaces/ui/SpaceEntryScreen.kt` |
| 09 / 14:672 | Вступить по ключу | `spaces/ui/SpaceEntryScreen.kt` |
| 10 / 14:696 | Входящие приглашения | `spaces/ui/InvitationsScreen.kt` |
| 11 / 14:730 | Пространство · администратор | `spaces/ui/SpaceDetailsScreen.kt` |
| 12 / 14:761 | Участники | `spaces/ui/SpaceMembersScreen.kt` |
| 13 / 14:815 | Роль участника | `spaces/ui/MemberRoleScreen.kt` |
| 14 / 14:856 | Приглашение по логину и ключ | `spaces/ui/InviteMemberScreen.kt` |
| 15 / 14:887 | Настройки пространства | `spaces/ui/SpaceSettingsScreen.kt` |
| 16 / 14:913 | Новый личный план | `event/ui/EventEditorScreen.kt` |
| 17 / 14:956 | Видимость по пространствам | `event/ui/EventVisibilityScreen.kt` |
| 18 / 14:995 | Выбор видимости | `event/ui/EventVisibilityScreen.kt` (SpaceVisibilityScreen) |
| 19 / 14:1025 | План внутри пространства | `event/ui/EventEditorScreen.kt` |
| 20 / 14:1059 | Подробности личного плана | `event/ui/EventDetailsScreen.kt` |
| 21 / 14:1094 | Только занятость | `event/ui/EventDetailsScreen.kt` |
| 22 / 14:1119 | Профиль | `profile/ui/ProfileScreen.kt` |
| 23 / 14:1161 | Редактирование профиля | `profile/ui/ProfileEditorScreen.kt` |
| 24 / 14:1201 | Даты, весь день, статус, повтор | `event/ui/EventDatesScreen.kt` |
| 25 / 14:1249 | Выход последнего администратора | `spaces/ui/LeaveSpaceScreen.kt` |
| 26 / 14:1276 | Пространство · посетитель | `spaces/ui/SpaceDetailsScreen.kt` |
| 27 / 14:1307 | Подтверждение удаления пространства | `spaces/ui/DeleteSpaceDialog.kt` |
| 28 / 14:1331 | Нет пространств | `spaces/ui/SpacesScreen.kt` |

Управление аккаунтами глобальным администратором в `users/ui/` дополняет макет теми же компонентами. Глобальная роль не даёт доступ к чужим планам.

## Desktop Web

В SVG четыре состояния широкого браузера. Их Compose-реализация находится в
`shared/src/commonMain/kotlin/com/project/whobusy/presentation/desktop/ui/`:

| Состояние SVG | Реализация |
| --- | --- |
| Общая доска · неделя | `DesktopCalendarScreen.kt`, `DesktopTimeCalendar.kt`, `DesktopCalendarToolbar.kt`; навигационная rail, пространства/фильтры, мини-календарь, часовая сетка и карточки |
| Доска · месяц и подробности плана | `DesktopMonthCalendar.kt`, `DesktopPlanDetailsScreen.kt`; без выбранного плана справа agenda выбранного дня |
| Пространство · участники и приглашения | `DesktopSpacesScreen.kt`, `DesktopSpaceNavigation.kt`, `DesktopSpaceDetailsScreen.kt`, `DesktopSpaceInvitePanel.kt`; выбор пространства, участники/ожидающие приглашения, реальные роли, приглашение по логину, ключ и настройки |
| Создание/редактирование плана | `DesktopEventEditorScreen.kt`, `DesktopEventFields.kt`, `DesktopEventVisibilityPanel.kt`; поля и видимость рядом, общий EditorState и сохранение |

Широкий профиль — согласованное развитие того же Telegram-стиля, а не пятое состояние SVG:
`DesktopProfileScreen.kt`, `DesktopProfileSidebar.kt`, `DesktopProfileDetailsPanel.kt`,
`DesktopProfileActionsPanel.kt`. Слева — аватар с инициалами, имя, логин, глобальная роль
и переходы; справа под синей шапкой — две колонки карточек личных данных/цвета планов
и приглашений/безопасности. Используются общие WhoBusySectionCard, кнопки и Material Icons.
Поля и приглашения берутся из ProfileState, отсутствующие необязательные данные обозначены
«Не указано». Действия открывают существующие редактор профиля, приглашения и управление
аккаунтами; последнее видно только глобальному admin. Списка устройств и вымышленной
статистики нет. При выходе действия и rail блокируются, подтверждение logout-all общее
с мобильным профилем. В низком окне sidebar и основная область прокручиваются отдельно.

`desktopLayout` — Web-возможность, не признак отдельной desktop-платформы. При ширине
меньше 1160 dp сайт использует существующий мобильный интерфейс с bottom bar, а Android не меняется.
На широком авторизованном Web rail постоянна; sidebar календаря скрывается при
недостаточной ширине, центральная область гибкая. Календарь начинает с недели,
сохраняет день/неделю/месяц и общие маршруты/права. Карточка открывает боковые
подробности; редактор/пространства используют существующие ViewModel без отдельного
набора данных. Числа, имена, роли и даты из SVG — пример, не hardcoded содержимое UI.

Вкладка пространств сразу использует широкую компоновку: sidebar и реальные карточки
пространств, приглашения, пустое состояние и повтор загрузки после ошибки. Детали
открываются только после выбора пользователя. Действия и подсказки sidebar
прокручиваются вместе со списком в низком окне. Мутация пространства временно
блокирует переходы через его UI, общую rail и Back; мобильная компоновка сохранена.
На вложенных формах без desktop-компоновки rail видима, но приглушена и
отключена; форма закрывается своими Back/Save, без обхода выполняемой операции.

SVG хранится только как исходный референс: рабочий интерфейс не является картинкой,
иконки берутся через UiIcon/Material Icons. Чистые desktop-экраны имеют автономные
Compose Preview. Результаты сборок и проверок не следуют из наличия Preview и
отмечаются отдельно в карте проекта.

## Токены, компоненты и адаптация

- Мобильная основа: 390 × 844. Используются dp/sp, системные отступы Android, прокрутка и IME padding. Верхняя панель — 68 dp плюс системный status inset (в макете вместе 92 px). Системные часы/батарея/жестовая полоса остаются нативными. Широкий Web использует desktop-референс и собственную rail/sidebar вместо растянутого bottom bar.
- Primary `#2481CC`, header `#517FA4`, text `#17212B`, secondary `#72808D`, background `#F1F3F5`, divider `#E7EBEF`. Цвета владельцев и светлые заливки — в общей теме `shared/src/commonMain/kotlin/com/project/whobusy/core/ui/theme/`.
- Кнопки высотой 50 dp, радиус 12 dp; поля с подписью высотой от 64 dp, радиус 12 dp; notice с радиусом 12 dp. По указанию пользователя поля входа и регистрации используют стиль Filled: светло-голубая заливка, синяя обводка 1 dp и тёмные подписи; при ошибке обводка и подпись красные. Общие строки списка, заголовки разделов, палитра и выбор радиокнопки находятся в `shared/src/commonMain/kotlin/com/project/whobusy/core/ui/components/`.
- Иконки адаптированы к стандартному набору [Material Icons для Compose](https://developer.android.com/develop/ui/compose/resources#icons): общий `UiIcon` отображает `ImageVector` из `Icons.Filled`, стрелка назад использует `Icons.AutoMirrored.Filled.ArrowBack`. Размеры значков и областей нажатия задаются компонентами; маркеры выбора — Material CheckCircle/RadioButtonUnchecked. Для текущих значков сторонних исключений нет. Новые иконки выбираются из Material; при отсутствии подходящей ищется готовый значок в интернете с указанным источником, без собственного рисования.
- Экспорты Figma сохранены в `shared/src/commonMain/composeResources/files/design/` как архив источника. Цветовые свотчи и белый thumb переключателя остаются декоративными ресурсами в `shared/src/commonMain/composeResources/drawable/`. Скриншоты макета не используются как UI.
- Внутри приложения компонент WhoBusyLogo использует предоставленный `WhoBusy.png`: неизменённый исходник хранится в `shared/src/commonMain/composeResources/drawable/whobusy_logo.png` и отображается на входе и при восстановлении сессии. Для адаптивного значка рабочего стола отдельно используется пользовательский `WhoBusyAndroidAppLogo.png`, без изменений сохранённый в `androidApp/src/main/res/drawable-nodpi/whobusy_launcher_logo.png`; предметная календарная иллюстрация пустой доски остаётся отдельной.
- Для дат/времени применяются общие Material 3 Compose picker-диалоги; преобразование выбранного дня выполняется через UTC без сдвига даты. Интервалы all-day хранят конец исключительно, редактор показывает последний включённый день.
- Плавающая панель сохраняет поведение [GameHub / BottomControl](https://github.com/Gaalexx/GameHub/blob/master/app/src/main/java/com/project/gamehub/presentation/navigation/BottomControl.kt): ширина 80%, высота 64 dp, радиус 25 dp, подвижный полупрозрачный индикатор. Три вкладки: Доска, Пространства, Профиль.
- Панель рисуется поверх `NavDisplay`, по бокам и ниже видно содержимое. Отступ снизу 10 dp плюс системный inset. Только маршруты с маркером `ScreenTypes.BottomBarNavigatable` показывают бар; формы, настройки и детали его не показывают.
- Даты, число пространств, роли и содержимое — динамические данные сервера. Только в явном demo seed-пространства названы «Команда WhoBusy», «Семья», «Проект», «Друзья», а демособытия привязаны к текущему дню.
- Строки, изображения и декоративные drawable общие: `shared/src/commonMain/composeResources/`, `com.project.whobusy.resources.Res` и Compose Resources. Системный значок запуска и ресурсы Android-host остаются в `androidApp/src/main/res/`.
- Все чистые экраны и новые визуальные компоненты имеют автономные Compose Preview. Проверки сборки и выполнения сценариев описаны в [AGENT_MAP.md](AGENT_MAP.md).
