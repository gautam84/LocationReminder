package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
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

@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {


    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private var appContext: Application =
        InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
    private lateinit var fakeDataSource: FakeDataSource


    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun init() {
        stopKoin()
        fakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(appContext, fakeDataSource)
    }

    @Test
    fun saveReminder_isRemindersListEmpty_false() {
        saveReminderViewModel.saveReminder(
            ReminderDataItem(
                "test",
                "test",
                "test",
                0.0,
                0.0,
                "test"
            )
        )
        assertThat(fakeDataSource.remainders.isEmpty(), `is`(false))
    }

    @Test
    fun onClear_areMutableDataCleared_true() {
        saveReminderViewModel.reminderTitle.value = "test"
        saveReminderViewModel.reminderDescription.value = "test"
        saveReminderViewModel.reminderSelectedLocationStr.value = "test"
        saveReminderViewModel.selectedPOI.value = PointOfInterest(LatLng(0.0, 0.0), "test", "test")
        saveReminderViewModel.latitude.value = 0.0
        saveReminderViewModel.longitude.value = 0.0

        saveReminderViewModel.onClear()

        assertThat(saveReminderViewModel.reminderTitle.value, `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderDescription.value, `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.value, `is`(nullValue()))
        assertThat(saveReminderViewModel.selectedPOI.value, `is`(nullValue()))
        assertThat(saveReminderViewModel.latitude.value, `is`(nullValue()))
        assertThat(saveReminderViewModel.longitude.value, `is`(nullValue()))

    }

    @Test
    fun validateReminder_isRemindersValid_true() {
        assertThat(
            saveReminderViewModel.validateEnteredData(
                ReminderDataItem(
                    "test",
                    "test",
                    "test",
                    0.0,
                    0.0,
                    "test"
                )
            ), `is`(true)
        )
    }

    @Test
    fun showSnackBarInt_validateEnteredData_true() {
        saveReminderViewModel.validateEnteredData(
            ReminderDataItem(
                null,
                "test",
                "test",
                0.0,
                0.0,
                "test"
            )
        )
        assertThat(saveReminderViewModel.showSnackBar.value, not(notNullValue()))
    }

    @Test
    fun showToast_isToastCorrect() {
        saveReminderViewModel.saveReminder(
            ReminderDataItem(
                "test",
                "test",
                "test",
                0.0,
                0.0,
                "test"
            )
        )
        assertThat(
            saveReminderViewModel.showToast.getOrAwaitValue(),
            `is`(appContext.getString(R.string.reminder_saved))
        )
    }

    @Test
    fun showLoading_Loading_false() {
        saveReminderViewModel.saveReminder(
            ReminderDataItem(
                "test",
                "test",
                "test",
                0.0,
                0.0,
                "test"
            )
        )
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }
}