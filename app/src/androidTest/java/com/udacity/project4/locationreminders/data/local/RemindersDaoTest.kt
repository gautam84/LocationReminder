package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()


    @Test
    fun saveRemainderToRemaindersDatabase() = runBlockingTest {
        database.reminderDao().saveReminder(
            ReminderDTO(
                "Test1", "test1", "test1", 0.0, 0.0, "test1"
            )
        )
        assertThat(database.reminderDao().getReminders(), notNullValue())
        assertThat(
            database.reminderDao().getReminders().contains(
                ReminderDTO(
                    "Test1", "test1", "test1", 0.0, 0.0, "test1"
                )
            ), `is`(true)
        )
    }

    @Test
    fun getAllRemindersFromRemaindersDatabase() = runBlockingTest {
        database.reminderDao().saveReminder(
            ReminderDTO(
                "Test1", "test1", "test1", 0.0, 0.0, "test1"
            )
        )
        assertThat(database.reminderDao().getReminders(), notNullValue())
    }

    @Test
    fun retrieveFromRemaindersDatabase() = runBlockingTest {
        val reminder1 = ReminderDTO("Test1", "test1", "test1", 0.0, 0.0, "test1")
        database.reminderDao().saveReminder(reminder1)

        assertThat(database.reminderDao().getReminderById(reminder1.id), notNullValue())
        assertThat(
            database.reminderDao().getReminderById(reminder1.id)?.title,
            `is`(reminder1.title)
        )
        assertThat(
            database.reminderDao().getReminderById(reminder1.id)?.description,
            `is`(reminder1.description)
        )
        assertThat(
            database.reminderDao().getReminderById(reminder1.id)?.latitude,
            `is`(reminder1.latitude)
        )
        assertThat(
            database.reminderDao().getReminderById(reminder1.id)?.longitude,
            `is`(reminder1.longitude)
        )


    }

    @Test
    fun deleteRemainderFromRemaindersDatabase() = runBlockingTest {
        database.reminderDao()
            .saveReminder(ReminderDTO("Test1", "test1", "test1", 0.0, 0.0, "test1"))
        assertThat(database.reminderDao().getReminders().isEmpty(),`is`(false))
        database.reminderDao().deleteAllReminders()
        assertThat(database.reminderDao().getReminders().isEmpty(),`is`(true))
    }
}