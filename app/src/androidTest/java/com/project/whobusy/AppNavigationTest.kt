package com.project.whobusy

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Rule
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

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
            composeRule.onNodeWithText("Профиль").performClick()
            composeRule.onNodeWithText("Выйти из аккаунта").performScrollTo().performClick()
        }
    }

    @Test
    fun loginCalendarModesFiltersAndLogout() {
        screenshot("01-login")
        login()
        screenshot("02-day")
        composeRule.onNodeWithText("Неделя").performClick()
        screenshot("03-week")
        composeRule.onNodeWithText("Месяц").performClick()
        screenshot("04-month")
        composeRule.onNodeWithText("Календари").performClick()
        composeRule.onNodeWithText("Анна Смирнова").performClick()
        composeRule.waitForIdle()
        screenshot("05-calendars")
        composeRule.onNodeWithText("Профиль").performClick()
        composeRule.onNodeWithText("Выйти из аккаунта").performScrollTo().performClick()
        composeRule.waitUntil(10_000) { composeRule.onAllNodesWithText("Войти").fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNodeWithText("Календарь занятости").assertIsDisplayed()
    }

    @Test
    fun selectedModeSurvivesRecreationAndBackReturnsToCalendar() {
        login()
        composeRule.onNodeWithText("Месяц").performClick()
        composeRule.activityRule.scenario.recreate()
        composeRule.onNodeWithText("Месяц").assertIsSelected()
        composeRule.onNodeWithText("Профиль").performClick()
        pressBack()
        composeRule.onNodeWithText("Месяц").assertIsDisplayed()
    }

    @Test
    fun canCreateAndDeleteOwnEvent() {
        login()
        composeRule.onNodeWithContentDescription("Создать событие").performClick()
        composeRule.onNodeWithText("Название").performTextInput("Тестовое событие")
        composeRule.onNodeWithText("Создать событие").performScrollTo().performClick()
        composeRule.waitUntil(10_000) { composeRule.onAllNodesWithText("Новое событие").fetchSemanticsNodes().isEmpty() }
        composeRule.onNodeWithText("Месяц").performClick()
        composeRule.onNodeWithText("День").performClick()
        composeRule.onNodeWithText("Тестовое событие").performScrollTo().performClick()
        composeRule.onNodeWithText("Удалить событие").performScrollTo().performClick()
        composeRule.onNodeWithText("Удалить", useUnmergedTree = true).performClick()
        composeRule.waitUntil(10_000) { composeRule.onAllNodesWithText("Тестовое событие").fetchSemanticsNodes().isEmpty() }
    }

    private fun login() {
        composeRule.onNodeWithText("Войти").performScrollTo().performClick()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("Синхронизация команды").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.waitForIdle()
    }

    private fun screenshot(name: String) {
        composeRule.waitForIdle()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.getExternalFilesDir(null), "screenshots").apply { mkdirs() }
        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
        File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
