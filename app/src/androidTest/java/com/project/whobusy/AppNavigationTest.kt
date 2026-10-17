package com.project.whobusy

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
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
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("Войти").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithText("Профиль").fetchSemanticsNodes().isNotEmpty()
        }
        if (composeRule.onAllNodesWithText("Войти").fetchSemanticsNodes().isEmpty()) {
            navigationTab("Профиль").performClick()
            logout()
        } else {
            waitForSignedOut()
        }
    }

    @Test
    fun loginCalendarModesFiltersAndLogout() {
        assertBottomBarHidden()
        screenshot("01-login")
        login()
        assertCalendarSelected()
        assertBarOverlays("calendar-content")
        screenshot("02-day")
        composeRule.onNodeWithText("Неделя").performClick()
        assertBarOverlays("calendar-content")
        screenshot("03-week")
        composeRule.onNodeWithText("Месяц").performClick()
        assertBarOverlays("calendar-content")
        screenshot("04-month")
        navigationTab("Календари").performClick()
        navigationTab("Календари").assertIsSelected()
        navigationTab("Календарь").assertIsNotSelected()
        composeRule.onNodeWithText("Анна Смирнова").performClick()
        composeRule.waitForIdle()
        assertBarOverlays("calendars-content")
        screenshot("05-calendars")
        navigationTab("Профиль").performClick()
        composeRule.onNodeWithTag("bottom-navigation").assertIsDisplayed()
        screenshot("06-profile")
        logout()
        composeRule.onNodeWithText("Календарь занятости").assertIsDisplayed()
        assertBottomBarHidden()
    }

    @Test
    fun selectedModeSurvivesRecreationAndBackReturnsToCalendar() {
        login()
        composeRule.onNodeWithText("Месяц").performClick()
        composeRule.activityRule.scenario.recreate()
        composeRule.onNodeWithText("Месяц").assertIsSelected()
        navigationTab("Календари").performClick()
        navigationTab("Календари").performClick()
        navigationTab("Календари").assertIsSelected()
        navigationTab("Профиль").performClick()
        navigationTab("Профиль").performClick()
        navigationTab("Профиль").assertIsSelected()
        composeRule.activityRule.scenario.recreate()
        navigationTab("Профиль").assertIsSelected()
        composeRule.onNodeWithTag("bottom-navigation").assertIsDisplayed()
        pressBack()
        assertCalendarSelected()
        composeRule.onNodeWithText("Месяц").assertIsSelected()
        navigationTab("Календари").performClick()
        composeRule.activityRule.scenario.recreate()
        navigationTab("Календари").assertIsSelected()
        pressBack()
        assertCalendarSelected()
        composeRule.onNodeWithText("Месяц").assertIsSelected()
    }

    @Test
    fun canCreateAndDeleteOwnEvent() {
        login()
        composeRule.onNodeWithContentDescription("Создать событие").performClick()
        assertBottomBarHidden()
        screenshot("07-editor")
        composeRule.onNodeWithText("Название").performTextInput("Тестовое событие")
        composeRule.onNodeWithText("Создать событие").performScrollTo().performClick()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("Новое событие").fetchSemanticsNodes().isEmpty()
        }
        assertCalendarSelected()
        composeRule.onNodeWithText("Месяц").performClick()
        composeRule.onNodeWithText("День").performClick()
        openCreatedEvent()
        assertBottomBarHidden()
        screenshot("08-details")
        composeRule.activityRule.scenario.recreate()
        composeRule.onNodeWithText("Тестовое событие").assertIsDisplayed()
        assertBottomBarHidden()
        composeRule.onNodeWithText("Редактировать событие").performScrollTo().performClick()
        composeRule.onNodeWithText("Изменить событие").assertIsDisplayed()
        composeRule.onNodeWithText("Название").assertTextContains("Тестовое событие")
        assertBottomBarHidden()
        composeRule.onNodeWithContentDescription("Назад").performClick()
        composeRule.onNodeWithText("Тестовое событие").assertIsDisplayed()
        assertBottomBarHidden()
        pressBack()
        assertCalendarSelected()
        openCreatedEvent()
        assertBottomBarHidden()
        composeRule.onNodeWithText("Удалить событие").performScrollTo().performClick()
        composeRule.onNodeWithText("Удалить", useUnmergedTree = true).performClick()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("Тестовое событие").fetchSemanticsNodes().isEmpty()
        }
        assertCalendarSelected()
    }

    @Test
    fun editorDraftSurvivesRecreationAndBackDiscardsIt() {
        login()
        composeRule.onNodeWithContentDescription("Создать событие").performClick()
        assertBottomBarHidden()
        composeRule.onNodeWithText("Название").performTextInput("Несохранённый черновик")
        composeRule.onNodeWithText("Место").performScrollTo().performTextInput("Переговорная")
        closeSoftKeyboard()
        composeRule.activityRule.scenario.recreate()
        assertBottomBarHidden()
        composeRule.onNodeWithText("Название").assertTextContains("Несохранённый черновик")
        composeRule.onNodeWithText("Место").assertTextContains("Переговорная")
        screenshot("09-editor-restored")
        pressBack()
        assertCalendarSelected()
        composeRule.onNodeWithText("Несохранённый черновик").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Создать событие").performClick()
        assertBottomBarHidden()
        composeRule.onNodeWithText("Несохранённый черновик").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Назад").performClick()
        assertCalendarSelected()
    }

    private fun login() {
        composeRule.onNodeWithText("Войти").performScrollTo().performClick()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("Синхронизация команды").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.waitForIdle()
    }

    private fun logout() {
        composeRule.onNodeWithText("Выйти из аккаунта").performScrollTo()
        composeRule.onNodeWithTag("profile-content").performTouchInput { swipeUp() }
        composeRule.onNodeWithText("Выйти из аккаунта").performClick()
        waitForSignedOut()
    }

    private fun waitForSignedOut() {
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("Войти").fetchSemanticsNodes().isNotEmpty() &&
                composeRule.onAllNodesWithTag("bottom-navigation").fetchSemanticsNodes().isEmpty()
        }
        composeRule.waitForIdle()
    }

    private fun openCreatedEvent() {
        composeRule
            .onNodeWithText("Тестовое событие")
            .performScrollTo()
            .performSemanticsAction(SemanticsActions.OnClick) { it() }
    }

    private fun navigationTab(title: String) = composeRule.onNode(hasText(title) and isSelectable())

    private fun assertCalendarSelected() {
        composeRule.onNodeWithTag("bottom-navigation").assertIsDisplayed()
        navigationTab("Календарь").assertIsSelected()
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
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.getExternalFilesDir(null), "screenshots").apply { mkdirs() }
        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
        File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
