package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import com.udacity.project4.locationreminders.data.dto.Result

import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeRemindersLocalRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    private lateinit var fakeRemindersLocalRepository: FakeRemindersLocalRepository
    private lateinit var appContext: Application
    private lateinit var reminderListViewModel: RemindersListViewModel
    private lateinit var reminderDTO : ReminderDTO



    @Before
    fun init() {
        appContext = ApplicationProvider.getApplicationContext()
        fakeRemindersLocalRepository = FakeRemindersLocalRepository()
        reminderListViewModel = RemindersListViewModel(appContext, fakeRemindersLocalRepository)

        stopKoin()
        val myModule = module {
            single {
                reminderListViewModel
            }
        }
        startKoin {
            modules(listOf(myModule))
        }

        runBlocking {
            fakeRemindersLocalRepository.deleteAllReminders()
        }
    }

    @Test
    fun testForError() = runBlockingTest{
        fakeRemindersLocalRepository.setShouldReturnError(true)
        fakeRemindersLocalRepository.getReminders() as Result.Error

        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText("Error occurred..")))
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

    @Test
    fun testIfEmptyList() = runBlockingTest {
        fakeRemindersLocalRepository.setShouldReturnError(false)
        fakeRemindersLocalRepository.getReminders()

        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

    @Test
    fun testRVList() = runBlockingTest{
        reminderDTO = ReminderDTO("title test", "desc","location", 0.0, 0.0)
        fakeRemindersLocalRepository.saveReminder(reminderDTO)

        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(withText(reminderDTO.title)).check(matches(isDisplayed()))
        onView(withText(reminderDTO.description)).check(matches(isDisplayed()))
        onView(withText(reminderDTO.location)).check(matches(isDisplayed()))
    }

    @Test
    fun testNavigationFromReminderListFragmentToSaveReminderFragment() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.addReminderFAB)).perform(click())

        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

}