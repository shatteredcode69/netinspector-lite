# NetInspector: Android Network Diagnostic App

## Overview
NetInspector is a lightweight, native Android application designed to provide deep insights into your current Wi-Fi connection. Unlike basic Wi-Fi scanners, this app acts as a network detective, combining local hardware data, external database lookups, and active network probing to reveal:
1. **Wi-Fi Signal & Details:** Live RSSI (-dBm), channel, BSSID, and link speed.
2. **ISP Details:** Public IP, Internet Service Provider name, Autonomous System Number (ASN), and geographic region.
3. **Firewall Detection:** Active evaluation of local gateway defenses (stealth mode vs. closed ports) and outbound filtering.

Built entirely without heavy third-party UI frameworks (like Flutter) or bloated dependency injection libraries, it relies on **Jetpack Compose** and **Clean MVVM architecture** in Kotlin.

---

## Architecture Overview

The app follows a Clean Architecture approach with a single-activity Compose setup:

*   **Presentation Layer:** Single-Activity Compose architecture. ViewModels expose immutable `UiState` via Kotlin `StateFlow`.
*   **Domain Layer:** Decoupled UseCases handling single-responsibility business logic (e.g., `ScanFirewallStatusUseCase`).
*   **Data Layer:** Repositories combining Android platform services (`WifiManager`), an external IP-lookup REST client, and raw socket probing engines.
*   **Dependency Injection:** A lightweight **Service Locator / AppContainer** pattern keeps the APK small and build times fast.

---

## Folder Structure

```text
app/
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/netinspector/app/
│   │   │   ├── NetInspectorApp.kt             # Application class (initializes AppContainer)
│   │   │   ├── MainActivity.kt                # Single Activity hosting Compose entrypoint
│   │   │   │
│   │   │   ├── core/                          # Cross-cutting foundational modules
│   │   │   │   ├── di/AppContainer.kt         # Lightweight manual DI container
│   │   │   │   ├── network/SocketEngine.kt    # Low-level TCP connect timeout helper
│   │   │   │   ├── permission/PermissionHandler.kt
│   │   │   │   └── util/RssiConverter.kt
│   │   │   │
│   │   │   ├── data/                          # Data sources and repository implementations
│   │   │   │   ├── model/IspResponseDto.kt, PortProbeResult.kt
│   │   │   │   ├── remote/IspApiClient.kt     # Minimal HTTP client using OkHttp
│   │   │   │   ├── local/WifiLocalDataSource.kt
│   │   │   │   └── repository/WifiRepositoryImpl.kt, IspRepositoryImpl.kt, FirewallRepositoryImpl.kt
│   │   │   │
│   │   │   ├── domain/                        # Pure Kotlin business rules
│   │   │   │   ├── model/WifiInfo.kt, IspDetails.kt, FirewallAudit.kt
│   │   │   │   ├── repository/WifiRepository.kt, IspRepository.kt, FirewallRepository.kt
│   │   │   │   └── usecase/ObserveWifiSignalUseCase.kt, GetIspDetailsUseCase.kt, ScanFirewallStatusUseCase.kt
│   │   │   │
│   │   │   └── presentation/                  # UI Components
│   │   │       ├── theme/Color.kt, Theme.kt, Type.kt
│   │   │       ├── dashboard/
│   │   │       │   ├── DashboardScreen.kt     # Main screen composing all cards
│   │   │       │   ├── DashboardViewModel.kt
│   │   │       │   ├── DashboardUiState.kt
│   │   │       │   └── components/SignalMeterCard.kt, IspDetailCard.kt, FirewallAuditCard.kt
│   │   │       └── common/PermissionRationaleDialog.kt
```

---

## Required Permissions & Platform APIs

### AndroidManifest.xml Requirements
```xml
<!-- Required for external ISP & ASN lookup and socket probing -->
<uses-permission android:name="android.intent.action.INTERNET" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- Required to access SSID, BSSID, and RSSI -->
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

<!-- Required for Android 13 (API 33) and above for local network hardware info -->
<uses-permission 
    android:name="android.permission.NEARBY_WIFI_DEVICES" 
    android:usesPermissionFlags="neverForLocation" />
```

### Key Android APIs Used
*   **Wi-Fi Details:** `ConnectivityManager.NetworkCallback` with `NetworkCapabilities.TRANSPORT_WIFI` to read `signalStrength` (RSSI in dBm) and `WifiInfo`.
*   **ISP Metadata:** GET requests to `https://ipinfo.io/json` via `OkHttp`.
*   **Firewall Probing:** Raw `java.net.Socket` operations in Kotlin Coroutines. Distinguishes between `SocketTimeoutException` (Filtered/Stealth) and `ConnectException` (Closed/Unfiltered).

---

## Developer Prompt (For AI or Engineer Handoff)

*Copy and paste the following prompt to generate the implementation:*

> Act as a Senior Android Systems and Network Engineer. Build a lightweight, native Android application in Kotlin using Jetpack Compose and Clean MVVM architecture. The app inspects the active Wi-Fi connection, identifies the user's ISP details, and runs a diagnostic audit to detect active network firewalls.
> 
> Do not use Flutter, XML layouts, or heavy annotation processors like Dagger/Hilt. Use manual dependency injection via an AppContainer.
> 
> ### Tech Stack & Constraints
> 1. Language: Kotlin (Coroutines + StateFlow)
> 2. UI: Jetpack Compose with Material 3
> 3. Networking: OkHttp (lightweight) for HTTP requests; native Java/Kotlin java.net.Socket for socket diagnostics
> 4. Target SDK: Android 14+ (API 34), Min SDK: 26
> 
> ### Core Requirements
> 
> 1. **Wi-Fi & Signal Monitoring:**
>    - Use ConnectivityManager.NetworkCallback and WifiManager to dynamically track the active Wi-Fi connection.
>    - Collect and display: SSID, BSSID, RSSI (in -dBm with a visual gauge), Link Speed (Mbps), Frequency (MHz), and Wi-Fi Standard.
>    - Gracefully handle ACCESS_FINE_LOCATION and NEARBY_WIFI_DEVICES runtime permission requests.
> 
> 2. **ISP Details Extraction:**
>    - Identify the local gateway IP from LinkProperties.
>    - Fetch public IP, Autonomous System Number (ASN), Organization/ISP name, and geographic region via a lightweight call to https://ipwho.is/ or https://ipinfo.io/json.
>    - Include error handling for offline or metered states.
> 
> 3. **Firewall & Gateway Diagnostic Engine:**
>    - Implement an asynchronous probe engine using Coroutines (Dispatchers.IO) targeting the gateway IP.
>    - Probe key gateway services (DNS: 53, SSH: 22, HTTP: 80, HTTPS: 443, Management: 8080/8443).
>    - Differentiate connection results:
>      * Connected -> Port Open
>      * SocketTimeoutException -> Port Filtered / Dropped (Firewall Active)
>      * ConnectException (Connection Refused) -> Port Closed / Unfiltered
>    - Run an outbound test against known public IP ports to evaluate egress filtering.
> 
> ### Code Quality Deliverables
> - Provide the complete AndroidManifest.xml with required permissions.
> - Implement the AppContainer for dependency injection.
> - Implement the SocketEngine and WifiLocalDataSource.
> - Implement DashboardViewModel exposing a sealed UI state.
> - Implement the primary Jetpack Compose UI (DashboardScreen) with cleanly separated composables for Wi-Fi Signal, ISP Details, and Firewall Status.