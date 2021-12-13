package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {
    var remainders: MutableList<ReminderDTO> = mutableListOf()
    var shouldReturnError = false


    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (shouldReturnError) {
            Result.Error("Error occurred")
        } else {
            Result.Success(remainders)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remainders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val reminder = remainders.find { it.id == id }
        return if (shouldReturnError) {
            Result.Error("Error occurred")
        } else {
            Result.Success(reminder!!)
        }
    }

    override suspend fun deleteAllReminders() {
        remainders.clear()
    }




}