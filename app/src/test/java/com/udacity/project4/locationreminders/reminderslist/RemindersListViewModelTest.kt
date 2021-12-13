package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.util.MainCoroutineRule
import com.udacity.project4.locationreminders.util.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])
class RemindersListViewModelTest {

    private lateinit var remindersListViewModel: RemindersListViewModel
    private var appContext: Application = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application


    private lateinit var fakeDataSource: FakeDataSource
    private val reminder1 = ReminderDTO("Test1", "test1", "test1", 0.0, 0.0, "test1")
    private val reminder2 = ReminderDTO("Test2", "test2", "test2", 0.0, 0.0, "test2")
    private val reminder3 = ReminderDTO("Test3", "test3", "test3", 0.0, 0.0, "test3")


    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun init() {
        stopKoin()
        fakeDataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(appContext, fakeDataSource)
    }

    @Test
    fun loadRemainder_reminderListEmpty_false() = mainCoroutineRule.runBlockingTest {

        fakeDataSource.saveReminder(
            reminder1
        )
        fakeDataSource.saveReminder(
            reminder2
        )
        fakeDataSource.saveReminder(
            reminder3
        )
        remindersListViewModel.loadReminders()
        val isEmpty = remindersListViewModel.remindersList.getOrAwaitValue()
        assertThat(isEmpty.isEmpty(), `is`(false))
    }

    @Test
    fun loadRemainder_showNoData_false() = mainCoroutineRule.runBlockingTest {

        fakeDataSource.saveReminder(
            reminder1
        )
        fakeDataSource.saveReminder(
            reminder2
        )
        fakeDataSource.saveReminder(
            reminder3
        )
        remindersListViewModel.loadReminders()
        remindersListViewModel.remindersList.getOrAwaitValue()
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun loadRemainders_ErrorOccurs_true(){
        fakeDataSource.shouldReturnError = true
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`(notNullValue()))
    }

    @Test
    fun loadRemainders_showsSnackbar_true() = runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))
    }


}