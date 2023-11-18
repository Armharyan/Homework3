package com.example.homework3

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class LocationPermissionHandler(private val context: Context) : ViewModel() {

    private val permissionRequest = Channel<Boolean>()
    val permissionResult = permissionRequest.receiveAsFlow()

    fun checkLocationPermission(activity: ComponentActivity) {
        if (hasLocationPermission()) {
            viewModelScope.launch {
                permissionRequest.send(true)
            }
        } else {
            requestLocationPermission(activity)
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission(activity: ComponentActivity) {
        val requestPermissionLauncher =
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                viewModelScope.launch {
                    permissionRequest.send(isGranted)
                }
            }

        requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }
}