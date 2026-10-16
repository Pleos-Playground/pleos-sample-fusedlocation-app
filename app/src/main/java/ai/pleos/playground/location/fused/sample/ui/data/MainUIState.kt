package ai.pleos.playground.location.fused.sample.ui.data

enum class PermissionState {
    CHECKING,
    GRANTED,
    DENIED
}

enum class ProximityAlertState {
    IDLE,
    REGISTERING,
    REGISTERED,
    REMOVED,
    ERROR
}

data class LocationSnapshot(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val heading: Float,
    val confidence: Float,
    val timestampMillis: Long
)

data class ProximityAlertArea(
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float,
)

data class MainUIState(
    val permissionState: PermissionState = PermissionState.CHECKING,
    val isSupported: Boolean? = null,
    val isInitialized: Boolean = false,
    val isSubscribed: Boolean = false,
    val latestLocation: LocationSnapshot? = null,
    val updateCount: Int = 0,
    val proximityAlertState: ProximityAlertState = ProximityAlertState.IDLE,
    val proximityAlertId: String? = null,
    val proximityAlertArea: ProximityAlertArea? = null,
    val lastProximityTransition: String? = null,
    val proximityEventCount: Int = 0,
    val errorMessage: String? = null
)
