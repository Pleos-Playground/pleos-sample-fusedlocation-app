package ai.pleos.playground.location.fused.sample

import ai.pleos.playground.location.fused.sample.ui.screen.MainScreen
import ai.pleos.playground.location.fused.sample.ui.theme.FusedLocationSampleAppTheme
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = viewModel::onPermissionResult
            )

            LaunchedEffect(Unit) {
                if (!viewModel.checkSupported()) return@LaunchedEffect

                if (hasFusedLocationPermission()) {
                    viewModel.onPermissionResult(true)
                } else {
                    viewModel.onPermissionResult(false)
                    permissionLauncher.launch(FUSED_LOCATION_PERMISSION_NAME)
                }
            }

            FusedLocationSampleAppTheme {
                MainScreen(
                    uiState = viewModel.uiState.collectAsStateWithLifecycle().value,
                    onRequestPermission = {
                        permissionLauncher.launch(FUSED_LOCATION_PERMISSION_NAME)
                    },
                    onStartUpdates = viewModel::startLocationUpdates,
                    onStopUpdates = viewModel::stopLocationUpdates,
                    onGetLastLocation = viewModel::getLastKnownLocation,
                    onCheckSupported = { viewModel.checkSupported() },
                    onDismissError = viewModel::dismissError,
                    onRegisterProximityAlert = viewModel::registerProximityAlert,
                    onRemoveProximityAlert = viewModel::removeProximityAlert,
                    onSimulateProximityInside = viewModel::simulateProximityInside,
                    onSimulateProximityOutside = viewModel::simulateProximityOutside,
                )
            }
        }
    }

    private fun hasFusedLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            FUSED_LOCATION_PERMISSION_NAME
        ) == PackageManager.PERMISSION_GRANTED

    companion object {
        private const val FUSED_LOCATION_PERMISSION_NAME =
            "pleos.car.permission.FUSED_LOCATION"
    }
}
