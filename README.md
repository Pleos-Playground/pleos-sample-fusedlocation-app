# Pleos FusedLocation SDK Android Sample

A sample Android app that demonstrates how to use the Pleos FusedLocation SDK.

## What this sample covers

- Checking service availability and initializing and releasing `FusedLocation`
- Requesting the last known location and starting and stopping continuous updates
- Registering and removing proximity alerts and receiving ENTER / EXIT events

## Requirements

- Android device or emulator environment where the Pleos FusedLocation service is available and supports proximity alerts.
- FusedLocation SDK dependencies resolvable from the configured Maven repositories.
- FusedLocation permission declared in `AndroidManifest.xml` and granted at runtime.

## SDK setup

Configure the Pleos Playground Maven repository and SDK dependency:

```kotlin
maven("https://nexus-playground.pleos.ai/repository/maven-releases/")
implementation("ai.pleos.playground:FusedLocation:1.1.3")
```

The SDK and its transitive dependencies must be published to this repository before building the sample in a fresh environment.

Declare the permission in your manifest:

```xml
<uses-permission android:name="pleos.car.permission.FUSED_LOCATION" />
```

## Build and run

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n \
  ai.pleos.playground.location.fused.sample/.MainActivity
```

The app checks service availability with `isSupported()` before requesting permission or initializing the SDK. If the service is unavailable, it displays a message and leaves the feature buttons disabled. Otherwise, allow the FusedLocation permission when prompted; the SDK is initialized after permission is granted.

Use the support status button to repeat the service availability check.

### Test Proximity Alerts on Pleos

**Inside** and **Outside** inject virtual GPS coordinates for testing. Allow mock locations before using these buttons:

```bash
adb shell appops set --user current \
  ai.pleos.playground.location.fused.sample \
  android:mock_location allow
```

Use **Start live updates** and wait for a location, or select **Get last known location** to retrieve a cached position. If live updates do not deliver a location, try **Get last known location**; a cached position must be available.

Once coordinates appear, select **Register at latest position** and wait for **Status: Registered**.

Then test in the order **Inside → Outside → Inside**, waiting for the location update after each press.

## Send a test location to an emulator

After the emulator is running, start live updates in the app and run:

```bash
adb emu geo fix 127.0276 37.4979
```

`geo fix` expects longitude first and latitude second and is only available on emulators.
