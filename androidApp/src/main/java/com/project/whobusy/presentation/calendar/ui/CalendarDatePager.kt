package com.project.whobusy.presentation.calendar.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.calendar.model.CalendarPagerPeriod
import com.project.whobusy.presentation.calendar.model.monthTitle
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import java.time.LocalDate

@Composable
internal fun CalendarDatePager(
    period: CalendarPagerPeriod,
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (LocalDate) -> Unit,
) = key(period) {
    val pager = rememberPagerState(initialPage = period.page(selectedDate), pageCount = { period.pageCount })
    var preferredDate by rememberSaveable { mutableStateOf(selectedDate.toString()) }
    var observedDate by rememberSaveable { mutableStateOf(selectedDate.toString()) }
    var requestedDate by remember { mutableStateOf<LocalDate?>(null) }
    val currentDate by rememberUpdatedState(selectedDate)
    val onDateSelected by rememberUpdatedState(onSelectDate)

    LaunchedEffect(selectedDate, pager) {
        // Explicit date selection wins over a restored page or an unfinished gesture.
        // A date committed by this pager preserves the preferred weekday/day of month.
        if (requestedDate != selectedDate) {
            if (observedDate != selectedDate.toString()) preferredDate = selectedDate.toString()
            pager.scrollToPage(period.page(selectedDate))
        }
        observedDate = selectedDate.toString()
        requestedDate = null
        snapshotFlow { pager.settledPage.takeUnless { pager.isScrollInProgress } }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { page ->
                if (page != period.page(currentDate)) {
                    val date = period.date(page, LocalDate.parse(preferredDate))
                    requestedDate = date
                    onDateSelected(date)
                }
            }
    }

    HorizontalPager(
        state = pager,
        modifier = modifier,
        key = { it },
    ) { page ->
        val date = period.date(page, LocalDate.parse(preferredDate))
        Box(Modifier.fillMaxWidth().testTag("calendar-page-$period-${period.start(date)}")) {
            content(date)
        }
    }
}

@Preview(name = "Перелистывание месяцев", widthDp = 390)
@Composable
private fun CalendarDatePagerPreview() {
    WhoBusyTheme {
        CalendarDatePager(CalendarPagerPeriod.Month, CalendarPreviewData.date, {}) { date ->
            Text(monthTitle(date))
        }
    }
}
