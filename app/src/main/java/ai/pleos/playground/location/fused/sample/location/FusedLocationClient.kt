package ai.pleos.playground.location.fused.sample.location

import ai.pleos.playground.location.fused.FusedLocation
import ai.pleos.playground.location.fused.listener.FusedLocationListener
import ai.pleos.playground.location.fused.listener.ProximityAlertListener
import ai.pleos.playground.location.fused.model.ProximityAlertEvent
import ai.pleos.playground.location.fused.model.ProximityAlertRequest
import ai.pleos.playground.location.fused.model.LocationData
import android.annotation.SuppressLint
import android.location.provider.ProviderProperties
import android.location.Location
import android.location.LocationManager
import android.content.Context
import android.os.SystemClock
import android.util.Log
import java.io.File

class FusedLocationClient(
    context: Context,
    private val callback: Callback
) {

    interface Callback {
        fun onLocationChanged(location: LocationData?)
        fun onError(exception: Exception)
        fun onProximityAlertRegistered(alertId: String)
        fun onProximityTransition(event: ProximityAlertEvent)
        fun onProximityAlertError(exception: Exception)
    }

    private val fusedLocation = FusedLocation(context.applicationContext)
    private val locationManager =
        context.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var isInitialized = false
    private var isSubscribed = false
    private var isMockProviderReady = false
    private val mockProviderMarker =
        File(context.applicationContext.noBackupFilesDir, "mock-gps-provider")

    private val listener = object : FusedLocationListener {
        override fun onChanged(location: LocationData?) {
            callback.onLocationChanged(location)
        }

        override fun onError(exception: Exception) {
            callback.onError(exception)
        }
    }

    private val proximityAlertListener = object : ProximityAlertListener {
        override fun onRegistered(alertId: String) {
            callback.onProximityAlertRegistered(alertId)
        }

        override fun onTransition(event: ProximityAlertEvent) {
            callback.onProximityTransition(event)
        }

        override fun onError(exception: Exception) {
            callback.onProximityAlertError(exception)
        }
    }

    fun isSupported(): Boolean {
        return fusedLocation.isSupported().also {
            Log.i(TAG, "Fused Location Service isSupported: $it")
        }
    }

    fun initialize() {
        if (isInitialized) return

        removeMockProvider()
        fusedLocation.initialize()
        isInitialized = true
    }

    fun startUpdates() {
        check(isInitialized) { "FusedLocationClient must be initialized before use." }
        if (isSubscribed) return

        fusedLocation.registerFusedLocationCallback(listener)
        isSubscribed = true
    }

    fun stopUpdates() {
        if (!isSubscribed) return

        fusedLocation.unregisterFusedLocationCallback(listener)
        isSubscribed = false
    }

    fun getLastKnownLocation() {
        check(isInitialized) { "FusedLocationClient must be initialized before use." }
        fusedLocation.getLastKnownLocation(listener)
    }

    fun addProximityAlert(request: ProximityAlertRequest) {
        check(isInitialized) { "FusedLocationClient must be initialized before use." }
        fusedLocation.addProximityAlert(request, proximityAlertListener)
    }

    fun removeProximityAlert(alertId: String) {
        fusedLocation.removeProximityAlert(alertId)
    }

    fun setMockLocation(latitude: Double, longitude: Double) {
        runCatching {
            prepareMockProvider()
            val location = Location(LocationManager.GPS_PROVIDER).apply {
                this.latitude = latitude
                this.longitude = longitude
                accuracy = 5f
                time = System.currentTimeMillis()
                elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            }
            locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location)
        }.onFailure { error ->
            callback.onError(error as? Exception ?: Exception(error))
        }
    }

    fun release() {
        if (!isInitialized) return

        stopUpdates()
        fusedLocation.release()
        removeMockProvider()
        isInitialized = false
    }

    @SuppressLint("InlinedApi")
    private fun prepareMockProvider() {
        if (!isMockProviderReady) {
            runCatching {
                locationManager.addTestProvider(
                    LocationManager.GPS_PROVIDER,
                    false,
                    true,
                    false,
                    false,
                    true,
                    true,
                    true,
                    ProviderProperties.POWER_USAGE_LOW,
                    ProviderProperties.ACCURACY_FINE,
                )
            }.onFailure { error ->
                if (error !is IllegalArgumentException) throw error
            }
            mockProviderMarker.createNewFile()
            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true)
            isMockProviderReady = true
        }
    }

    private fun removeMockProvider() {
        if (!isMockProviderReady && !mockProviderMarker.exists()) return

        runCatching {
            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER)
            check(!mockProviderMarker.exists() || mockProviderMarker.delete()) {
                "Could not clear mock location provider state."
            }
            isMockProviderReady = false
        }.onFailure {
            Log.w(TAG, "Failed to remove mock location provider", it)
        }
    }

    private companion object {
        const val TAG = "FusedLocationSample"
    }
}
