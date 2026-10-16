package ai.pleos.playground.location.fused.sample.ui.screen

import ai.pleos.playground.location.fused.sample.ui.data.LocationSnapshot
import ai.pleos.playground.location.fused.sample.ui.data.MainUIState
import ai.pleos.playground.location.fused.sample.ui.data.PermissionState
import ai.pleos.playground.location.fused.sample.ui.data.ProximityAlertState
import ai.pleos.playground.location.fused.sample.ui.theme.FusedLocationSampleAppTheme
import ai.pleos.playground.location.fused.sample.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val SUPPORT_LABEL_SUPPORTED = "Supported"
private const val SUPPORT_LABEL_NOT_SUPPORTED = "Not supported"
private const val SUPPORT_LABEL_CHECK = "Check support"

@Composable
fun MainScreen(
    uiState: MainUIState,
    onRequestPermission: () -> Unit,
    onStartUpdates: () -> Unit,
    onStopUpdates: () -> Unit,
    onGetLastLocation: () -> Unit,
    onCheckSupported: () -> Unit,
    onDismissError: () -> Unit,
    onRegisterProximityAlert: () -> Unit,
    onRemoveProximityAlert: () -> Unit,
    onSimulateProximityInside: () -> Unit,
    onSimulateProximityOutside: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize().systemBarsPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Header(
                    uiState = uiState,
                    onCheckSupported = onCheckSupported
                )

                uiState.errorMessage?.let { message ->
                    ErrorBanner(message = message, onDismiss = onDismissError)
                }

                LocationCard(uiState.latestLocation, uiState.updateCount)
                LocationDetails(uiState.latestLocation)
                ControlsCard(
                    uiState = uiState,
                    onRequestPermission = onRequestPermission,
                    onStartUpdates = onStartUpdates,
                    onStopUpdates = onStopUpdates,
                    onGetLastLocation = onGetLastLocation
                )
                ProximityAlertCard(
                    uiState = uiState,
                    onRegister = onRegisterProximityAlert,
                    onRemove = onRemoveProximityAlert,
                    onSimulateInside = onSimulateProximityInside,
                    onSimulateOutside = onSimulateProximityOutside,
                )
                DeveloperCard()
            }
        }
    }
}

@Composable
private fun Header(
    uiState: MainUIState,
    onCheckSupported: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "FUSEDLOCATION",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Location dashboard",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "A focused sample for integrating and validating the Pleos FusedLocation SDK.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(20.dp))
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusBadge(uiState)
            OutlinedButton(onClick = onCheckSupported) {
                Text(
                    text = when (uiState.isSupported) {
                        true -> SUPPORT_LABEL_SUPPORTED
                        false -> SUPPORT_LABEL_NOT_SUPPORTED
                        null -> SUPPORT_LABEL_CHECK
                    },
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(uiState: MainUIState) {
    val (label, color) = when {
        uiState.isSupported == false ->
            "Not supported" to MaterialTheme.colorScheme.error
        uiState.permissionState == PermissionState.DENIED ->
            "Permission required" to MaterialTheme.colorScheme.error
        uiState.isSubscribed -> "Live updates" to MaterialTheme.colorScheme.primary
        uiState.isInitialized -> "Ready" to MaterialTheme.colorScheme.tertiary
        else -> "Initializing" to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(100),
        color = color.copy(alpha = 0.12f),
        contentColor = color
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun LocationCard(location: LocationSnapshot?, updateCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(28.dp)) {
            Text(
                text = "LATEST RECEIVED POSITION",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(20.dp))

            if (location == null) {
                Text(
                    text = "Waiting for location",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Start live updates or request the last known location.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
                )
            } else {
                CoordinateRow("LATITUDE", formatCoordinate(location.latitude))
                Spacer(Modifier.height(14.dp))
                CoordinateRow("LONGITUDE", formatCoordinate(location.longitude))
                Spacer(Modifier.height(22.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.14f)
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Updated ${formatTimestamp(location.timestampMillis)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
                    )
                    Text(
                        text = "$updateCount samples",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun CoordinateRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.62f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LocationDetails(location: LocationSnapshot?) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                label = "ALTITUDE",
                value = location?.let { String.format(Locale.US, "%.1f m", it.altitude) } ?: "—",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "HEADING",
                value = location?.let { String.format(Locale.US, "%.1f°", it.heading) } ?: "—",
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                label = "CONFIDENCE",
                value = location?.let { String.format(Locale.US, "%.1f", it.confidence) } ?: "—",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "TIMESTAMP",
                value = location?.let { formatTimestamp(it.timestampMillis) } ?: "—",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ControlsCard(
    uiState: MainUIState,
    onRequestPermission: () -> Unit,
    onStartUpdates: () -> Unit,
    onStopUpdates: () -> Unit,
    onGetLastLocation: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Controls",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (uiState.isSubscribed) {
                    "The SDK is delivering continuous location updates."
                } else {
                    "Choose continuous updates or fetch a single cached position."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(22.dp))

            if (uiState.permissionState == PermissionState.DENIED) {
                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Grant location permission")
                }
            } else {
                Button(
                    onClick = if (uiState.isSubscribed) onStopUpdates else onStartUpdates,
                    enabled = uiState.isInitialized,
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (uiState.isSubscribed) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    } else {
                        ButtonDefaults.buttonColors()
                    }
                ) {
                    Text(if (uiState.isSubscribed) "Stop live updates" else "Start live updates")
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onGetLastLocation,
                    enabled = uiState.isInitialized,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Get last known location")
                }

            }
        }
    }
}

@Composable
private fun ProximityAlertCard(
    uiState: MainUIState,
    onRegister: () -> Unit,
    onRemove: () -> Unit,
    onSimulateInside: () -> Unit,
    onSimulateOutside: () -> Unit,
) {
    val isRegistered = uiState.proximityAlertId != null
    val isRegistering = uiState.proximityAlertState == ProximityAlertState.REGISTERING
    val area = uiState.proximityAlertArea
    val status = when (uiState.proximityAlertState) {
        ProximityAlertState.IDLE -> "Not registered"
        ProximityAlertState.REGISTERING -> stringResource(R.string.proximity_registering)
        ProximityAlertState.REGISTERED -> "Registered"
        ProximityAlertState.REMOVED -> "Removed"
        ProximityAlertState.ERROR -> "Error"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Proximity alert",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.proximity_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = if (area != null) {
                    stringResource(R.string.proximity_area, area.latitude, area.longitude, area.radiusMeters)
                } else {
                    stringResource(R.string.proximity_center_hint)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status: $status",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Events: ${uiState.proximityEventCount}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            uiState.proximityAlertId?.let { alertId ->
                Spacer(Modifier.height(8.dp))
                SelectionContainer {
                    Text(
                        text = "Alert ID: $alertId",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Last transition: ${uiState.lastProximityTransition ?: "—"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onRegister,
                enabled = uiState.isInitialized && uiState.latestLocation != null &&
                    !isRegistered && !isRegistering,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(if (isRegistering) R.string.proximity_registering else R.string.proximity_register))
            }
            if (uiState.latestLocation == null) {
                Text(stringResource(R.string.proximity_location_required))
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onRemove,
                enabled = isRegistered,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Remove proximity alert")
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = stringResource(R.string.proximity_mock_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onSimulateOutside,
                    enabled = isRegistered,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Outside")
                }
                OutlinedButton(
                    onClick = onSimulateInside,
                    enabled = isRegistered,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Inside")
                }
            }
        }
    }
}

@Composable
private fun DeveloperCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Try it on an emulator",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Select this app as the emulator's mock location app once. Then use the Inside and Outside buttons in the Proximity alert card.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer
    ) {
        Row(
            modifier = Modifier.padding(start = 18.dp, top = 10.dp, end = 8.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}

internal fun formatCoordinate(value: Double): String =
    String.format(Locale.US, "%.6f", value)

private fun formatTimestamp(timestampMillis: Long): String =
    SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date(timestampMillis))

@Preview(widthDp = 1200, heightDp = 720, showBackground = true)
@Composable
private fun MainScreenWidePreview() {
    FusedLocationSampleAppTheme {
        MainScreen(
            uiState = MainUIState(
                permissionState = PermissionState.GRANTED,
                isInitialized = true,
                isSubscribed = true,
                latestLocation = LocationSnapshot(
                    latitude = 37.4979,
                    longitude = 127.0276,
                    altitude = 38.2,
                    heading = 112.4f,
                    confidence = 0.9f,
                    timestampMillis = 1_754_000_000_000
                ),
                updateCount = 24
            ),
            onRequestPermission = {},
            onStartUpdates = {},
            onStopUpdates = {},
            onGetLastLocation = {},
            onCheckSupported = {},
            onDismissError = {},
            onRegisterProximityAlert = {},
            onRemoveProximityAlert = {},
            onSimulateProximityInside = {},
            onSimulateProximityOutside = {},
        )
    }
}

@Preview(widthDp = 412, heightDp = 915, showBackground = true)
@Composable
private fun MainScreenPhonePreview() {
    FusedLocationSampleAppTheme {
        MainScreen(
            uiState = MainUIState(permissionState = PermissionState.DENIED),
            onRequestPermission = {},
            onStartUpdates = {},
            onStopUpdates = {},
            onGetLastLocation = {},
            onCheckSupported = {},
            onDismissError = {},
            onRegisterProximityAlert = {},
            onRemoveProximityAlert = {},
            onSimulateProximityInside = {},
            onSimulateProximityOutside = {},
        )
    }
}
