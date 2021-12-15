package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings


class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent =
            Intent(requireActivity().applicationContext, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(
            requireActivity().applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)


        binding.viewModel = _viewModel

        geofencingClient = LocationServices.getGeofencingClient(activity!!)

        return binding.root
    }

    @SuppressLint("NewApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            checkDeviceLocationSettingsAndStartGeofence()

            Log.d("tagg", _viewModel.selectedPOI.value.toString())

        }
    }

    @SuppressLint("NewApi")
    fun checkDeviceLocationSettingsAndStartGeofence(){
        val title = _viewModel.reminderTitle.value
        val description = _viewModel.reminderDescription.value
        val location = _viewModel.reminderSelectedLocationStr.value
        val latitude = _viewModel.latitude.value
        val longitude = _viewModel.longitude.value

        val reminderDataItem = ReminderDataItem(
            title,
            description,
            location,
            latitude,
            longitude
        )

        if (reminderDataItem.latitude != null &&
            reminderDataItem.longitude != null &&
            reminderDataItem.location != null
        ) {
            val geofence = createGeofence(
                reminderDataItem.latitude!!,
                reminderDataItem.longitude!!,
                reminderDataItem.id
            )
            val geofencingRequest = createGeofenceRequest(geofence)

            if (isPermissionGranted()) {
                val locationManager =
                    requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

                if (locationManager.isLocationEnabled) {
                    addGeofence(geofencingRequest, geofencePendingIntent)

                } else {

                    val locationRequest = LocationRequest.create().apply {
                        priority = LocationRequest.PRIORITY_LOW_POWER
                    }
                    val builder =
                        LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
                    val settingsClient = LocationServices.getSettingsClient(requireActivity())
                    val locationSettingsResponseTask =
                        settingsClient.checkLocationSettings(builder.build())
                    locationSettingsResponseTask.addOnFailureListener { exception ->
                        if (exception is ResolvableApiException) {
                            try {
                                startIntentSenderForResult(
                                    exception.resolution.intentSender,
                                    REQUEST_TURN_DEVICE_LOCATION_ON,
                                    null,
                                    0,
                                    0,
                                    0,
                                    null
                                )
                            } catch (sendEx: IntentSender.SendIntentException) {
                                Log.d(
                                    "TAG",
                                    "Error getting location settings resolution: " + sendEx.message
                                )
                            }
                        } else {
                            Snackbar.make(
                                requireView(),
                                R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                            )
                        }
                    }
                }


            } else {
                requestBackgroundAndForegroundPermissions()
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSettingsAndStartGeofence()
        }
    }

    private fun requestBackgroundAndForegroundPermissions() {
        if (isPermissionGranted())
            return
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
            REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
        } else {
            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }
        requestPermissions(
            permissionsArray,
            resultCode
        )
    }


    private fun isPermissionGranted(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ))
        val backgroundPermissionApproved =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d("TAG", "onRequestPermissionResult")

        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED))
        {
            Snackbar.make(
                binding.root,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", "com.example.android.treasureHunt", null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        } else {
            checkDeviceLocationSettingsAndStartGeofence()
        }
    }

    private fun createGeofence(latitude: Double, longitude: Double, id: String): Geofence {
        return Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(latitude, longitude, GEOFENCE_RADIUS_IN_METERS)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()
    }

    private fun createGeofenceRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
    }


    @SuppressLint("MissingPermission", "LongLogTag")
    private fun addGeofence(
        geofencingRequest: GeofencingRequest,
        geofencePendingIntent: PendingIntent
    ) {
        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
            addOnSuccessListener {
                activity?.let {
                    Log.d("tag", "Added Geofence!")

                    val title = _viewModel.reminderTitle.value
                    val description = _viewModel.reminderDescription.value
                    val location = _viewModel.reminderSelectedLocationStr.value
                    val latitude = _viewModel.latitude.value
                    val longitude = _viewModel.longitude.value

                    _viewModel.validateAndSaveReminder(
                        ReminderDataItem(
                            title,
                            description,
                            location,
                            latitude,
                            longitude
                        )

                    )
                    findNavController().navigateUp()


                }
            }
            addOnFailureListener {
                activity?.let {
                    _viewModel.showToast.value = getString(R.string.geofences_not_added)
                }
                if ((it.message != null)) {
                    Log.d("Failure encountered adding Geofence: %s", it.message.toString())
                }
            }
        }
    }


    companion object {
        const val GEOFENCE_RADIUS_IN_METERS = 100f
        private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
        private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
        private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
        private const val LOCATION_PERMISSION_INDEX = 0
        private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
        internal const val ACTION_GEOFENCE_EVENT =
            "Geofence Action"
    }


}





