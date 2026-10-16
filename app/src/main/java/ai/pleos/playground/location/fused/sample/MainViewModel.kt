package ai.pleos.playground.location.fused.sample

import ai.pleos.playground.location.fused.model.LocationData
import ai.pleos.playground.location.fused.model.ProximityAlertEvent
import ai.pleos.playground.location.fused.model.ProximityAlertRequest
import ai.pleos.playground.location.fused.model.ProximityTransition
import ai.pleos.playground.location.fused.sample.location.FusedLocationClient
import ai.pleos.playground.location.fused.sample.ui.data.LocationSnapshot
import ai.pleos.playground.location.fused.sample.ui.data.MainUIState
import ai.pleos.playground.location.fused.sample.ui.data.PermissionState
import ai.pleos.playground.location.fused.sample.ui.data.ProximityAlertArea
import ai.pleos.playground.location.fused.sample.ui.data.ProximityAlertState
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel(application: Application) :
    AndroidViewModel(application),
    FusedLocationClient.Callback {

    private val _uiState = MutableStateFlow(MainUIState())
    val uiState = _uiState.asStateFlow()

    private val fusedLocationClient = FusedLocationClient(application, this)

    fun checkSupported(): Boolean {
        val isSupported = fusedLocationClient.isSupported()
        _uiState.update {
            it.copy(
                isSupported = isSupported,
                errorMessage = when {
                    !isSupported -> getString(R.string.service_unavailable_message)
                    it.isSupported == false -> null
                    else -> it.errorMessage
                }
            )
        }
        return isSupported
    }

    fun onPermissionResult(isGranted: Boolean) {
        if (_uiState.value.isSupported != true && !checkSupported()) return

        if (!isGranted) {
            _uiState.update {
                it.copy(
                    permissionState = PermissionState.DENIED,
                    errorMessage = getString(R.string.permission_required_message)
                )
            }
            return
        }

        fusedLocationClient.initialize()
        _uiState.update {
            it.copy(
                permissionState = PermissionState.GRANTED,
                isInitialized = true,
                errorMessage = null
            )
        }
    }

    fun startLocationUpdates() {
        if (!canUseFusedLocation() || _uiState.value.isSubscribed) return

        fusedLocationClient.startUpdates()
        _uiState.update {
            it.copy(isSubscribed = true, errorMessage = null)
        }
    }

    fun stopLocationUpdates() {
        if (!_uiState.value.isSubscribed) return

        fusedLocationClient.stopUpdates()
        _uiState.update {
            it.copy(isSubscribed = false, errorMessage = null)
        }
    }

    fun getLastKnownLocation() {
        if (!canUseFusedLocation()) return
        fusedLocationClient.getLastKnownLocation()
    }

    fun registerProximityAlert() {
        if (!canUseFusedLocation()) return

        val state = _uiState.value
        if (state.proximityAlertId != null ||
            state.proximityAlertState == ProximityAlertState.REGISTERING
        ) return

        val location = state.latestLocation
        if (location == null || !location.hasValidCoordinates()) {
            _uiState.update { it.copy(errorMessage = getString(R.string.proximity_location_required)) }
            return
        }

        val area = ProximityAlertArea(location.latitude, location.longitude, PROXIMITY_RADIUS_METERS)

        _uiState.update {
            it.copy(
                proximityAlertState = ProximityAlertState.REGISTERING,
                proximityAlertArea = area,
                lastProximityTransition = null,
                proximityEventCount = 0,
                errorMessage = null
            )
        }
        try {
            fusedLocationClient.addProximityAlert(
                ProximityAlertRequest(
                    latitude = area.latitude,
                    longitude = area.longitude,
                    radiusMeters = area.radiusMeters,
                    transitionTypes = setOf(
                        ProximityTransition.ENTER,
                        ProximityTransition.EXIT
                    )
                )
            )
        } catch (exception: Exception) {
            onProximityAlertError(exception)
        }
    }

    fun removeProximityAlert() {
        val alertId = _uiState.value.proximityAlertId ?: return
        fusedLocationClient.removeProximityAlert(alertId)
        _uiState.update {
            it.copy(
                proximityAlertState = ProximityAlertState.REMOVED,
                proximityAlertId = null,
                proximityAlertArea = null,
                lastProximityTransition = null,
                proximityEventCount = 0
            )
        }
    }

    fun simulateProximityInside() {
        if (!canUseFusedLocation()) return

        val state = _uiState.value
        val area = state.proximityAlertArea ?: return
        if (state.proximityAlertId == null) return

        fusedLocationClient.setMockLocation(area.latitude, area.longitude)
    }

    fun simulateProximityOutside() {
        if (!canUseFusedLocation()) return

        val state = _uiState.value
        val area = state.proximityAlertArea ?: return
        if (state.proximityAlertId == null) return

        val latitude = area.latitude + Math.toDegrees(
            area.radiusMeters * OUTSIDE_RADIUS_MULTIPLIER / EARTH_RADIUS_METERS
        )
        val crossesPole = latitude > 90.0
        val longitude = if (crossesPole) (area.longitude + 360.0) % 360.0 - 180.0 else area.longitude

        fusedLocationClient.setMockLocation(
            latitude = if (crossesPole) 180.0 - latitude else latitude,
            longitude = longitude
        )
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onLocationChanged(location: LocationData?) {
        if (location == null) {
            _uiState.update {
                it.copy(errorMessage = getString(R.string.location_unavailable_message))
            }
            return
        }

        _uiState.update {
            it.copy(
                latestLocation = location.toSnapshot(),
                updateCount = it.updateCount + 1,
                errorMessage = null
            )
        }
    }

    override fun onError(exception: Exception) {
        _uiState.update {
            it.copy(
                errorMessage = exception.message
                    ?: getString(R.string.unknown_error_message)
            )
        }
    }

    override fun onProximityAlertError(exception: Exception) {
        _uiState.update {
            it.copy(
                proximityAlertState = ProximityAlertState.ERROR,
                proximityAlertArea = if (it.proximityAlertId == null) null else it.proximityAlertArea,
                errorMessage = exception.message
                    ?: getString(R.string.unknown_error_message)
            )
        }
    }

    override fun onProximityAlertRegistered(alertId: String) {
        _uiState.update {
            it.copy(
                proximityAlertState = ProximityAlertState.REGISTERED,
                proximityAlertId = alertId,
                errorMessage = null
            )
        }
    }

    override fun onProximityTransition(event: ProximityAlertEvent) {
        _uiState.update {
            if (event.alertId != it.proximityAlertId) return@update it
            it.copy(
                lastProximityTransition = event.transition.name,
                proximityEventCount = it.proximityEventCount + 1,
                errorMessage = null
            )
        }
    }

    override fun onCleared() {
        fusedLocationClient.release()
        super.onCleared()
    }

    private fun canUseFusedLocation(): Boolean {
        val canUse = _uiState.value.permissionState == PermissionState.GRANTED &&
            _uiState.value.isInitialized
        if (!canUse) {
            _uiState.update {
                it.copy(errorMessage = getString(R.string.permission_required_message))
            }
        }
        return canUse
    }

    private fun LocationSnapshot.hasValidCoordinates(): Boolean {
        return latitude.isFinite() && longitude.isFinite() &&
            latitude in -90.0..90.0 && longitude in -180.0..180.0
    }

    private fun LocationData.toSnapshot() = LocationSnapshot(
        latitude = latitude,
        longitude = longitude,
        altitude = altitude,
        heading = heading,
        confidence = confidence,
        timestampMillis = timestampMillis
    )

    private fun getString(resId: Int): String = getApplication<Application>().getString(resId)

    private companion object {
        const val PROXIMITY_RADIUS_METERS = 100f
        const val OUTSIDE_RADIUS_MULTIPLIER = 3.0
        const val EARTH_RADIUS_METERS = 6_371_000.0
    }
}
