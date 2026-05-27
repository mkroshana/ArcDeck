# ArcDeck

A homelab mission control dashboard for Android. Monitor and manage your self-hosted infrastructure — Proxmox VE, Unraid, and *Arr services — from a single app.

## Features

- **Proxmox VE** — View VMs/containers, resource usage, and execute power actions (start/stop/shutdown)
- **Unraid** — Monitor storage pools via GraphQL
- **Arr Stack** — Track Sonarr/Radarr download queues
- **Live Telemetry** — CPU, RAM, storage, and network stats updated in real-time
- **Service Tracker** — Add, remove, and toggle homelab services with ping and uptime tracking
- **Terminal Logs** — System event log with per-source filtering
- **Demo Mode** — Preview the dashboard with simulated data (enabled by default)

## Prerequisites

- [Android Studio](https://developer.android.com/studio)
- Android device or emulator (API 24+)

## Setup

1. Open the project in Android Studio and let Gradle sync.

2. In [app/build.gradle.kts](app/build.gradle.kts), remove this line from the `debug` build type:
   ```kotlin
   signingConfig = signingConfigs.getByName("debugConfig")
   ```

3. Run on an emulator or physical device.

## Configuration

On first launch the app starts in **Demo Mode** with simulated data. Open Settings within the app to connect to your real services:

| Service    | Required fields                          |
|------------|------------------------------------------|
| Proxmox VE | Base URL, API Token, Node name           |
| Unraid     | GraphQL endpoint URL, Auth token         |
| Arr        | Base URL, API key                        |

## Tech Stack

Kotlin · Jetpack Compose · Material 3 · Room · Retrofit · OkHttp · Moshi
