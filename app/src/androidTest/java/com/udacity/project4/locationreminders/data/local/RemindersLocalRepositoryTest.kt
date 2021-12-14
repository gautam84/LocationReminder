package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.util.MainCoroutineRule
import junit.framework.Assert.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var remainderRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun init() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        remainderRepository = RemindersLocalRepository(database.reminderDao())
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun saveRemainderFunctionSuccess() = runBlocking {
        val reminder = ReminderDTO(
            "Test1", "test1", "test1", 0.0, 0.0, "test1"
        )
        remainderRepository.saveReminder(
            reminder
        )
        val result = remainderRepository.getReminder(reminder.id)
        assertThat(result is Result.Success, `is`(true))
        result as Result.Success
        assertThat(result.data.id, `is`(reminder.id))
        assertThat(result.data.title, `is`(notNullValue()))
        assertThat(result.data.description, `is`(notNullValue()))
        assertThat(result.data.latitude, `is`(notNullValue()))
        assertThat(result.data.longitude, `is`(notNullValue()))
    }


    @Test
    fun getRemainders_isReminderListEmpty_false() = runBlocking {
        remainderRepository.saveReminder(
            ReminderDTO(
                "Test1", "test1", "test1", 0.0, 0.0, "test1"
            )
        )
        remainderRepository.getReminders()
        val list = remainderRepository.getReminders() as Result.Success<List<ReminderDTO>>
        assertThat(list.data.isNotEmpty(), `is`(true))
    }

    @Test
    fun getReminder_isReminderListEmpty_false() = runBlocking {
        remainderRepository.saveReminder(
            ReminderDTO(
                "Test1", "test1", "test1", 0.0, 0.0, "test1"
            )
        )
        val list = remainderRepository.getReminder("test1")
        assertThat(list, `is`(notNullValue()))
    }

    @Test
    fun deleteReminders_isReminderListEmpty_false() = runBlocking {
        remainderRepository.saveReminder(
            ReminderDTO(
                "Test1", "test1", "test1", 0.0, 0.0, "test1"
            )
        )
        val list = remainderRepository.getReminders() as Result.Success<List<ReminderDTO>>
        assertThat(list.data.isNotEmpty(), `is`(true))
        remainderRepository.deleteAllReminders()
        val savedList = remainderRepository.getReminders() as Result.Success<List<ReminderDTO>>
        assertThat(savedList.data.isEmpty(), `is`(true))
    }

    @Test
    fun reminderNotFound_isReminderStatusCodeNull_true() = runBlocking {
        remainderRepository.saveReminder(
            ReminderDTO(
                "Test1", "test1", "test1", 0.0, 0.0, "test1"
            )
        )
        val reminder = remainderRepository.getReminder("unknown id") as Result.Error
        assertNotNull(reminder)
        assertEquals("Reminder not found!", reminder.message)
        assertNull(reminder.statusCode)
    }


}