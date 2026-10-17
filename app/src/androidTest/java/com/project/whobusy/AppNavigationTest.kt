package com.project.whobusy

import android.graphics.Bitmap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class AppNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun startWithSignedOutUser() {
        composeRule.waitUntil(10_000) { exists("Войти") || hasBottomBar() }
        if (hasBottomBar()) {
            navigationTab("Профиль").performClick()
            logout()
        }
        waitForSignedOut()
    }

    @Test
    fun boardFiltersMainTabsProfileAndRestoration() {
        screenshot("01-login")
        login()
        assertBoardSelected()
        assertBarOverlays("calendar-content")
        screenshot("03-board-day")
        click("Неделя")
        screenshot("05-board-week")
        click("Месяц")
        screenshot("06-board-month")
        composeRule.activityRule.scenario.recreate()
        composeRule.onNodeWithText("Месяц").assertIsSelected()
        click("День")

        composeRule.onNodeWithContentDescription("Настройки доски").performClick()
        assertBottomBarHidden()
        waitForText("Что показывать")
        screenshot("04-board-settings")
        click("Только мои")
        composeRule.activityRule.scenario.recreate()
        composeRule.onNodeWithText("Мои планы").assertIsDisplayed()
        composeRule.onNodeWithText("Команда WhoBusy").assertIsOff()
        click("Показать доску")
        waitForBottomBar()
        assertBoardSelected()
        composeRule.onNodeWithContentDescription("Настройки доски").performClick()
        click("Общая")
        click("Показать доску")
        waitForBottomBar()

        navigationTab("Пространства").performClick()
        waitForText("Команда WhoBusy")
        navigationTab("Пространства").assertIsSelected()
        assertBarOverlays("spaces-content")
        screenshot("07-spaces")
        navigationTab("Пространства").performClick()
        composeRule.activityRule.scenario.recreate()
        navigationTab("Пространства").assertIsSelected()
        pressBack()
        assertBoardSelected()

        navigationTab("Профиль").performClick()
        assertBarOverlays("profile-content")
        screenshot("22-profile")
        click("Личные данные")
        waitForText("Логин нельзя изменить самостоятельно.")
        assertBottomBarHidden()
        screenshot("23-profile-edit")
        replaceField("Имя", "Навигация")
        closeSoftKeyboard()
        composeRule.activityRule.scenario.recreate()
        composeRule.onNodeWithText("Имя").assertTextContains("Навигация")
        click("Сохранить")
        waitForBottomBar()
        waitForText("Гайдуков Навигация")
        composeRule.activityRule.scenario.recreate()
        waitForBottomBar()
        navigationTab("Профиль").assertIsSelected()
        composeRule.onNodeWithText("Гайдуков Навигация").assertIsDisplayed()
        click("Личные данные")
        replaceField("Имя", "Александр")
        closeSoftKeyboard()
        click("Сохранить")
        waitForBottomBar()
        logout()
        assertBottomBarHidden()
    }

    @Test
    fun spacesRolesInvitationsLastAdminAndDeleteConfirmation() {
        login()
        navigationTab("Пространства").performClick()
        waitForText("Команда WhoBusy")
        click("Создать пространство")
        waitForText("Новое пространство")
        assertBottomBarHidden()
        screenshot("08-create-space")
        pressBack()
        click("Войти по ключу")
        waitForText("Войти в пространство")
        screenshot("09-join-space")
        pressBack()
        click("Приглашения")
        waitForText("Вы сами решаете, куда присоединиться.")
        screenshot("10-invitations")
        pressBack()

        click("Команда WhoBusy")
        waitForText("Открыть планы пространства")
        assertBottomBarHidden()
        screenshot("11-space-admin")
        click("Участники")
        waitForText("Дарья Орлова")
        screenshot("12-space-members")
        click("Дарья Орлова")
        waitForText("Роль участника")
        screenshot("13-member-role")
        click("Актор")
        click("Сохранить роль")
        waitForText("Дарья Орлова")
        click("Дарья Орлова")
        click("Визитор")
        click("Сохранить роль")
        waitForText("Дарья Орлова")
        pressBack()

        click("Настройки пространства")
        waitForText("Сохранить название")
        screenshot("15-space-settings")
        click("Выйти из пространства")
        waitForText("Вы единственный администратор")
        screenshot("25-leave-last-admin")
        composeRule.onNode(hasText("Назначить администратора") and hasClickAction()).assertIsDisplayed()
        pressBack()
        pressBack()
        pressBack()
        waitForBottomBar()

        click("Проект")
        waitForText("Ваш доступ")
        screenshot("26-space-visitor")
        click("Добавить свой план")
        waitForText("Новый план")
        assertBottomBarHidden()
        screenshot("19-plan-in-space")
        pressBack()
        pressBack()
        waitForBottomBar()

        val name = "UI-проект-" + System.currentTimeMillis()
        click("Создать пространство")
        replaceField("Название", name)
        closeSoftKeyboard()
        click("Создать пространство")
        waitForText("Открыть планы пространства")
        click("Пригласить")
        waitForText("ПО ЛОГИНУ · С ПОДТВЕРЖДЕНИЕМ")
        screenshot("14-invite-member")
        replaceField("Логин пользователя", "admin")
        closeSoftKeyboard()
        click("Отправить приглашение")
        waitForText("Приглашение отправлено")
        screenshot("14-invite-pending")
        click("Актор · Отозвать приглашение")
        composeRule.waitUntil(10_000) { !exists("Ожидает ответа: admin") }
        pressBack()
        click("Настройки пространства")
        click("Удалить пространство")
        waitForText("Удалить " + name + "?")
        screenshot("27-delete-space-dialog")
        composeRule.onAllNodes(hasText("Удалить пространство") and hasClickAction()).onLast().performClick()
        waitForBottomBar()
        navigationTab("Пространства").assertIsSelected()
        composeRule.onAllNodesWithText(name).assertCountEquals(0)
    }

    @Test
    fun personalPlanVisibilityDatesDraftAndFullOrBusyDetails() {
        login()
        openPlan("Работа над макетом")
        waitForText("Главный экран и навигация")
        assertBottomBarHidden()
        screenshot("20-plan-details-full")
        composeRule.activityRule.scenario.recreate()
        waitForText("Главный экран и навигация")
        composeRule.onNodeWithText("Работа над макетом").assertIsDisplayed()
        assertBottomBarHidden()
        pressBack()
        openPlan("Занято")
        waitForText("Подробности скрыты владельцем")
        screenshot("21-plan-details-busy")
        composeRule.onAllNodesWithText("Редактировать план").assertCountEquals(0)
        pressBack()

        composeRule.onNodeWithContentDescription("Создать план").performClick()
        waitForText("Новый план")
        val title = "UI-план-" + System.currentTimeMillis()
        replaceField("Название", title)
        replaceField("Место", "Переговорная")
        closeSoftKeyboard()
        screenshot("16-personal-plan")
        composeRule.activityRule.scenario.recreate()
        composeRule.onNodeWithText("Название").assertTextContains(title)
        composeRule.onNodeWithText("Место").assertTextContains("Переговорная")
        click("Видимость в пространствах")
        waitForText("Кто увидит план")
        screenshot("17-plan-visibility")
        click("Команда WhoBusy")
        waitForText("Для акторов и выше")
        screenshot("18-space-visibility")
        click("Для акторов и выше")
        click("Применить")
        waitForText("Кто увидит план")
        click("Готово")
        waitForText("Новый план")
        click("Начало → окончание")
        waitForText("Даты и повтор")
        composeRule.onAllNodes(isToggleable())[0].performClick()
        click("Свободен")
        screenshot("24-plan-dates-repeat")
        click("Готово")
        waitForText("Новый план")
        click("Создать план")
        waitForBottomBar()
        composeRule.activityRule.scenario.recreate()
        waitForBottomBar()
        assertBoardSelected()
        openPlan(title)
        waitForText("Переговорная")
        screenshot("20-plan-details-own")
        click("Редактировать план")
        waitForText("Изменить план")
        assertBottomBarHidden()
        composeRule.onNodeWithText("Название").assertTextContains(title)
        pressBack()
        click("Удалить план")
        click("Удалить")
        waitForBottomBar()
        assertBoardSelected()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText(title, substring = true).fetchSemanticsNodes().isEmpty()
        }
        composeRule.onAllNodesWithText(title, substring = true).assertCountEquals(0)
    }

    @Test
    fun registrationEmptyBoardAndEmptySpaces() {
        assertBottomBarHidden()
        click("Создать аккаунт")
        waitForText("Ваши пространства — ваши правила")
        screenshot("02-registration")
        replaceField("Логин", "uitest" + System.currentTimeMillis())
        replaceField("Фамилия", "Тестовый")
        replaceField("Имя", "Участник")
        replaceField("Пароль", "demo1234")
        closeSoftKeyboard()
        click("Зарегистрироваться")
        waitForBottomBar()
        waitForText("Пока никаких планов")
        screenshot("03-empty-board")
        navigationTab("Пространства").performClick()
        waitForText("Соберите своих людей")
        assertBarOverlays("spaces-content")
        screenshot("28-empty-spaces")
        navigationTab("Профиль").performClick()
        logout()
    }

    @Test
    fun acceptingInvitationAndJoiningByCodeUseDifferentRoles() {
        login()
        navigationTab("Пространства").performClick()
        click("Приглашения")
        waitForText("Друзья")
        screenshot("10-invitations-populated")
        click("Принять приглашение")
        waitForText("Ваш доступ")
        composeRule.onNodeWithText("Участников: 3 · Актор").assertIsDisplayed()
        screenshot("10-invitation-accepted")
        click("Выйти из пространства")
        click("Выйти из пространства")
        waitForBottomBar()
        click("Войти по ключу")
        replaceField("Пригласительный ключ", "WBFRIENDS2026")
        closeSoftKeyboard()
        click("Присоединиться")
        waitForText("Ваш доступ")
        composeRule.onNodeWithText("Участников: 3 · Визитор").assertIsDisplayed()
        screenshot("26-joined-by-key")
        click("Выйти из пространства")
        click("Выйти из пространства")
        waitForBottomBar()
    }

    @Test
    fun globalAdministratorCanOpenAccountScreensWithoutSpaceAccess() {
        login("admin", "admin123")
        waitForText("Пока никаких планов")
        navigationTab("Пространства").performClick()
        waitForText("Соберите своих людей")
        screenshot("28-admin-no-spaces")
        navigationTab("Профиль").performClick()
        click("Управление пользователями")
        waitForText("Управление аккаунтами")
        assertBottomBarHidden()
        screenshot("29-users")
        click("Создать пользователя")
        waitForText("ГЛОБАЛЬНАЯ РОЛЬ")
        screenshot("30-create-user")
        pressBack()
        reveal(composeRule.onNode(hasText("demo", substring = true) and hasClickAction())).performClick()
        waitForText("Редактировать пользователя")
        screenshot("31-edit-user")
        pressBack()
        pressBack()
        waitForBottomBar()
        logout()
    }

    private fun login(
        identifier: String = "demo",
        password: String = "demo1234",
    ) {
        replaceField("Логин или email", identifier)
        replaceField("Пароль", password)
        closeSoftKeyboard()
        click("Войти")
        waitForBottomBar()
        assertBoardSelected()
    }

    private fun logout() {
        click("Выйти из аккаунта")
        waitForSignedOut()
    }

    private fun waitForSignedOut() {
        composeRule.waitUntil(10_000) { exists("Войти") && !hasBottomBar() }
        composeRule.waitForIdle()
    }

    private fun waitForBottomBar() {
        composeRule.waitUntil(10_000) { hasBottomBar() }
        composeRule.waitForIdle()
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(10_000) { exists(text) }
        composeRule.waitForIdle()
    }

    private fun exists(text: String): Boolean = composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()

    private fun hasBottomBar(): Boolean =
        composeRule.onAllNodesWithTag("bottom-navigation").fetchSemanticsNodes().isNotEmpty()

    private fun click(text: String) {
        val node = composeRule.onNode(hasText(text) and hasClickAction())
        reveal(node)
            .assertIsEnabled()
            .performSemanticsAction(SemanticsActions.OnClick) { it() }
        composeRule.waitForIdle()
    }

    private fun replaceField(
        label: String,
        value: String,
    ) {
        reveal(composeRule.onNodeWithText(label)).performTextReplacement(value)
    }

    private fun reveal(node: SemanticsNodeInteraction): SemanticsNodeInteraction {
        try {
            node.assertIsDisplayed()
        } catch (_: AssertionError) {
            node.performScrollTo()
        }
        return node
    }

    private fun openPlan(title: String) {
        val matcher = hasText(title, substring = true) and hasClickAction()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty()
        }
        val node = composeRule.onAllNodes(matcher)[0]
        reveal(node).performSemanticsAction(SemanticsActions.OnClick) { it() }
        composeRule.waitForIdle()
    }

    private fun navigationTab(title: String) = composeRule.onNode(hasText(title) and isSelectable())

    private fun assertBoardSelected() {
        composeRule.onNodeWithTag("bottom-navigation").assertIsDisplayed()
        navigationTab("Доска").assertIsSelected()
    }

    private fun assertBottomBarHidden() {
        composeRule.onNodeWithTag("bottom-navigation").assertDoesNotExist()
    }

    private fun assertBarOverlays(contentTag: String) {
        composeRule.onNodeWithTag("bottom-navigation").assertIsDisplayed()
        val rootBounds = composeRule.onRoot().fetchSemanticsNode().boundsInRoot
        val barBounds = composeRule.onNodeWithTag("bottom-navigation").fetchSemanticsNode().boundsInRoot
        val contentBounds = composeRule.onNodeWithTag(contentTag).fetchSemanticsNode().boundsInRoot
        assertTrue("Navigation bar must leave content visible on the left", barBounds.left > rootBounds.left)
        assertTrue("Navigation bar must leave content visible on the right", barBounds.right < rootBounds.right)
        assertTrue("Navigation bar must leave content visible below", barBounds.bottom < contentBounds.bottom)
        assertTrue("Content must extend to the bottom of the screen", contentBounds.bottom >= rootBounds.bottom - 1f)
    }

    private fun screenshot(name: String) {
        composeRule.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val directory = File(context.getExternalFilesDir(null), "screenshots").apply { mkdirs() }
        val bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
