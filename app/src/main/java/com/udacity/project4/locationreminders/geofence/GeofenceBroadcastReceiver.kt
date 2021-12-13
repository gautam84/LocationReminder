package com.udacity.project4.locationreminders.geofence

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity.Companion.ACTION_GEOFENCE_EVENT

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    @SuppressLint("LongLogTag")
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_GEOFENCE_EVENT) {
            // Obtain the Geofence event
            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            // Check if there is an error with the Geofence event and log the message
            if (geofencingEvent.hasError()) {
                val errorMessage = when (geofencingEvent.errorCode) {
                    GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> context.getString(R.string.geofence_not_available)
                    GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> context.getString(R.string.geofence_too_many_geofences)
                    GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> context.getString(R.string.geofence_too_many_pending_intents)
                    else -> context.getString(R.string.geofence_unknown_error)
                }
                Log.e("Error encountered in handing geofencing event: %s", errorMessage)
                return
            }

            // If the user has entered the Geofence
            if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

                // Log that the user has entered the Geofence
                Log.d("tag", context.getString(R.string.geofence_entered))

                when {
                    geofencingEvent.triggeringGeofences.isNotEmpty() -> {

                        // Log that the triggering Geofence(s) exist and use the IntentService to enqueue the work
                        Log.i(
                            "tag",
                            "Calling intent service to send notification the user has entered the Geofence..."
                        )
                        GeofenceTransitionsJobIntentService.enqueueWork(context, intent)
                    }
                    else -> {

                        // Log that the triggering Geofence(s) were empty and exit the process
                        Log.e("tag", "Triggering Geofences is empty. Exiting...")
                        return
                    }
                }
            }
        }
    }
}
