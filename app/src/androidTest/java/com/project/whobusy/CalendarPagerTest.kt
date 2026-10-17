package com.project.whobusy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.calendar.model.CalendarMode
import com.project.whobusy.presentation.calendar.model.dayTitle
import com.project.whobusy.presentation.calendar.model.weekStart
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.calendar.ui.CalendarScreen
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalendarPagerTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var selectedDate: MutableState<Long>
    private lateinit var selectedMode: MutableState<CalendarMode>
    private val commands = mutableListOf<CalendarCommand>()

    @Test
    fun dayContentSwipesToAdjacentDaysAndBack() {
        val initial = CalendarPreviewData.date
        showCalendar(initial, CalendarMode.Day)

        swipeForward("calendar-day-pager")
        assertDate(initial.plusDays(1))
        assertPage(CalendarMode.Day, initial.plusDays(1))

        swipeBack("calendar-day-pager")
        assertDate(initial)
        assertPage(CalendarMode.Day, initial)
        assertDateCommands(initial.plusDays(1), initial)
    }

    @Test
    fun dayDateStripSwipesWholeWeeksAndKeepsTheSelectedWeekday() {
        val initial = CalendarPreviewData.date
        showCalendar(initial, CalendarMode.Day)

        swipeForward("calendar-week-strip-pager")
        assertDate(initial.plusWeeks(1))
        assertPage(CalendarMode.Day, initial.plusWeeks(1))

        swipeBack("calendar-week-strip-pager")
        assertDate(initial)
        assertPage(CalendarMode.Day, initial)
        assertDateCommands(initial.plusWeeks(1), initial)
    }

    @Test
    fun weekSwipesMoveTheGridAndDatesTogetherAndDateCellsRemainClickable() {
        val initial = CalendarPreviewData.date
        showCalendar(initial, CalendarMode.Week)

        swipeForward("calendar-week-pager")
        assertDate(initial.plusWeeks(1))
        assertPage(CalendarMode.Week, initial.plusWeeks(1))

        swipeBack("calendar-week-pager")
        assertDate(initial)
        assertPage(CalendarMode.Week, initial)

        val friday = initial.plusDays(1)
        composeRule.onNodeWithContentDescription(dayTitle(friday)).assertIsDisplayed().performClick()
        assertDate(friday)
        swipeForward("calendar-week-pager")

        assertDate(friday.plusWeeks(1))
        assertPage(CalendarMode.Week, friday.plusWeeks(1))
        assertDateCommands(initial.plusWeeks(1), initial, friday, friday.plusWeeks(1))
    }

    @Test
    fun monthSwipesClampFebruaryWithoutLosingThePreferredDayOfMonth() {
        val january = LocalDate.of(2027, 1, 31)
        val february = LocalDate.of(2027, 2, 28)
        val march = LocalDate.of(2027, 3, 31)
        showCalendar(january, CalendarMode.Month)

        swipeForward("calendar-month-pager")
        assertDate(february)
        assertPage(CalendarMode.Month, february)
        swipeForward("calendar-month-pager")
        assertDate(march)
        assertPage(CalendarMode.Month, march)

        swipeBack("calendar-month-pager")
        assertDate(february)
        swipeBack("calendar-month-pager")
        assertDate(january)
        assertPage(CalendarMode.Month, january)
        assertDateCommands(february, march, february, january)
    }

    @Test
    fun todayResynchronizesThePagerAndTheNextSwipeStartsFromToday() {
        val initial = CalendarPreviewData.date
        val today = LocalDate.of(2027, 7, 7)
        showCalendar(initial, CalendarMode.Day, today)
        swipeForward("calendar-day-pager")
        assertDate(initial.plusDays(1))

        composeRule.onNodeWithText(text(R.string.action_today)).performClick()
        assertDate(today)
        assertPage(CalendarMode.Day, today)
        swipeForward("calendar-day-pager")

        assertDate(today.plusDays(1))
        assertPage(CalendarMode.Day, today.plusDays(1))
        assertDateCommands(initial.plusDays(1), today.plusDays(1))
        composeRule.runOnIdle { assertEquals(1, commands.count { it == CalendarCommand.Today }) }
    }

    @Test
    fun externalDateAndModeChangesResynchronizePagersWithoutEmittingDuplicateDates() {
        val initial = CalendarPreviewData.date
        val external = LocalDate.of(2027, 2, 12)
        showCalendar(initial, CalendarMode.Day)
        swipeForward("calendar-day-pager")

        composeRule.runOnIdle { selectedDate.value = external.toEpochDay() }
        assertPage(CalendarMode.Day, external)
        selectMode(CalendarMode.Month)
        assertPage(CalendarMode.Month, external)
        assertDateCommands(initial.plusDays(1))

        swipeForward("calendar-month-pager")
        assertDate(external.plusMonths(1))
        selectMode(CalendarMode.Day)
        assertPage(CalendarMode.Day, external.plusMonths(1))
        swipeForward("calendar-day-pager")

        assertDate(external.plusMonths(1).plusDays(1))
        assertDateCommands(initial.plusDays(1), external.plusMonths(1), external.plusMonths(1).plusDays(1))
    }

    @Test
    fun swipedMonthAndPreferredDaySurviveSavedStateRestorationWithoutDuplicateCommands() {
        val january = LocalDate.of(2027, 1, 31)
        val february = LocalDate.of(2027, 2, 28)
        val march = LocalDate.of(2027, 3, 31)
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { CalendarHarness(january, CalendarMode.Month, january) }
        composeRule.waitForIdle()
        swipeForward("calendar-month-pager")
        assertDate(february)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        assertDate(february)
        assertPage(CalendarMode.Month, february)
        assertDateCommands(february)
        swipeForward("calendar-month-pager")
        assertDate(march)
        assertPage(CalendarMode.Month, march)
        assertDateCommands(february, march)
    }

    @Test
    fun verticalTimelineGesturesScrollHoursWithoutChangingDayOrWeek() {
        val initial = CalendarPreviewData.date
        showCalendar(initial, CalendarMode.Day)
        assertVerticalScrollOnly("day-time-grid", CalendarMode.Day, initial)

        selectMode(CalendarMode.Week)
        assertVerticalScrollOnly("week-time-grid", CalendarMode.Week, initial)

        assertDate(initial)
        assertDateCommands()
    }

    private fun showCalendar(
        initialDate: LocalDate,
        initialMode: CalendarMode,
        today: LocalDate = CalendarPreviewData.date,
    ) {
        composeRule.setContent { CalendarHarness(initialDate, initialMode, today) }
        composeRule.waitForIdle()
        assertDateCommands()
    }

    @Composable
    private fun CalendarHarness(initialDate: LocalDate, initialMode: CalendarMode, today: LocalDate) {
        selectedDate = rememberSaveable { mutableStateOf(initialDate.toEpochDay()) }
        selectedMode = rememberSaveable { mutableStateOf(initialMode) }
        WhoBusyTheme {
            CalendarScreen(
                CalendarPreviewData.state.copy(
                    selectedDate = LocalDate.ofEpochDay(selectedDate.value),
                    mode = selectedMode.value,
                ),
                onEvent = { command ->
                    commands += command
                    when (command) {
                        is CalendarCommand.SelectDate -> selectedDate.value = command.date.toEpochDay()
                        is CalendarCommand.SelectMode -> selectedMode.value = command.mode
                        CalendarCommand.Today -> selectedDate.value = today.toEpochDay()
                        else -> Unit
                    }
                },
                now = CalendarPreviewData.date.minusDays(1).atStartOfDay(),
            )
        }
    }

    private fun swipeForward(pagerTag: String) {
        composeRule.onNodeWithTag(pagerTag).performTouchInput { swipeLeft(durationMillis = 450) }
        composeRule.waitForIdle()
    }

    private fun swipeBack(pagerTag: String) {
        composeRule.onNodeWithTag(pagerTag).performTouchInput { swipeRight(durationMillis = 450) }
        composeRule.waitForIdle()
    }

    private fun selectMode(mode: CalendarMode) {
        composeRule.onNodeWithText(text(mode.titleRes)).performClick()
        composeRule.waitForIdle()
    }

    private fun assertDate(date: LocalDate) {
        composeRule.runOnIdle { assertEquals(date, LocalDate.ofEpochDay(selectedDate.value)) }
    }

    private fun assertPage(mode: CalendarMode, date: LocalDate) {
        composeRule.onNodeWithTag(pageTag(mode, date), useUnmergedTree = true).assertIsDisplayed()
    }

    private fun assertDateCommands(vararg expected: LocalDate) {
        composeRule.runOnIdle {
            assertEquals(expected.toList(), commands.filterIsInstance<CalendarCommand.SelectDate>().map { it.date })
        }
    }

    private fun assertVerticalScrollOnly(gridTag: String, mode: CalendarMode, date: LocalDate) {
        val grid = composeRule.onNode(
            hasTestTag(gridTag) and hasAnyAncestor(hasTestTag(pageTag(mode, date))),
            useUnmergedTree = true,
        )
        val before = grid.scrollPosition()

        grid.performTouchInput {
            swipeUp(startY = height * 0.8f, endY = height * 0.3f, durationMillis = 700)
        }
        composeRule.waitForIdle()

        assertTrue("The vertical gesture must scroll the hours", grid.scrollPosition() > before)
        assertDate(date)
        assertPage(mode, date)
    }

    private fun SemanticsNodeInteraction.scrollPosition(): Float =
        fetchSemanticsNode().config[SemanticsProperties.VerticalScrollAxisRange].value()

    private fun pageTag(mode: CalendarMode, date: LocalDate): String {
        val period = when (mode) {
            CalendarMode.Day -> date
            CalendarMode.Week -> weekStart(date)
            CalendarMode.Month -> date.withDayOfMonth(1)
        }
        return "calendar-page-${mode.name}-$period"
    }

    private fun text(resource: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resource)
}
